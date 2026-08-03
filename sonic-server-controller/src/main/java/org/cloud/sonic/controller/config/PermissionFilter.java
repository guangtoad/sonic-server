/*
 *   sonic-server  Sonic Cloud Real Machine Platform.
 *   Copyright (C) 2022 SonicCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.cloud.sonic.controller.config;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.common.config.CommonResultControllerAdvice;
import org.cloud.sonic.common.http.RespEnum;
import org.cloud.sonic.common.http.RespModel;
import org.cloud.sonic.common.tools.JWTTokenTool;
import org.cloud.sonic.controller.models.domain.Resources;
import org.cloud.sonic.controller.models.interfaces.UrlType;
import org.cloud.sonic.controller.services.ResourcesService;
import org.cloud.sonic.controller.services.RolesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 权限过滤器 - 继承OncePerRequestFilter，实现基于角色的访问控制（RBAC）
 *
 * 过滤逻辑：
 * 1. Swagger文档请求直接放行
 * 2. 超管账号直接放行
 * 3. 检查资源是否存在
 * 4. 检查资源是否为白名单
 * 5. 检查用户是否有访问权限
 */
@Component
@Slf4j
public class PermissionFilter extends OncePerRequestFilter {

    /** 是否开启权限管理，通过配置项 sonic.permission.enable 控制 */
    @Value("${sonic.permission.enable}")
    private boolean permissionEnable;

    /** 内置超管账号，拥有所有权限，通过配置项 sonic.permission.superAdmin 控制 */
    @Value("${sonic.permission.superAdmin}")
    private String superAdmin;

    /** JWT Token工具类，用于解析Token获取用户名 */
    @Autowired
    private JWTTokenTool jwtTokenTool;

    /** 角色服务，用于检查用户权限 */
    @Autowired
    private RolesServices rolesServices;

    /** 资源服务，用于查询资源信息 */
    @Autowired
    private ResourcesService resourcesService;

    /** Token请求头名称常量 */
    public static final String TOKEN = "SonicToken";

    /** 国际化消息资源管理器 */
    @Resource
    private MessageSource messageSource;

    /**
     * 权限过滤核心方法 - 执行权限检查逻辑
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Swagger文档请求直接放行
        if (request.getRequestURL().toString().contains("swagger")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求头中的Token
        String token = request.getHeader(TOKEN);

        // 2. 如果开启权限管理且存在Token且不是OPTIONS请求，则进行权限检查
        if (permissionEnable && token != null && !request.getMethod().equalsIgnoreCase("options")) {
            // 从Token中解析用户名
            String userName = jwtTokenTool.getUserName(token);

            // 3. 超管账号直接放行
            if (Objects.equals(superAdmin, userName)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 获取请求路径和HTTP方法
            String resourceName = request.getServletPath();
            String method = request.getMethod();
            log.info("PermissionFilter: {}", resourceName);

            // 4. 查询资源是否存在
            Resources resources = resourcesService.search(resourceName, method);

            // 资源不存在，返回404
            if (resources == null) {
                response.setContentType("text/plain;charset=UTF-8");
                JSONObject re = (JSONObject) JSONObject.toJSON(process(request, new RespModel(RespEnum.RESOURCE_NOT_FOUND)));
                response.getWriter().write(re.toJSONString());
                return;
            }

            // 5. 检查资源是否为白名单（无需权限验证）
            if (resources.getWhite() == UrlType.WHITE || resources.getNeedAuth() == UrlType.WHITE) {
                filterChain.doFilter(request, response);
                return;
            }

            // 6. 检查用户是否有该资源的访问权限
            if (!rolesServices.checkUserHasResourceAuthorize(userName, resourceName, method)) {
                // 权限不足，返回403
                response.setContentType("text/plain;charset=UTF-8");
                JSONObject re = (JSONObject) JSONObject.toJSON(process(request, new RespModel(RespEnum.PERMISSION_DENIED)));
                response.getWriter().write(re.toJSONString());
                return;
            }
        }

        // 所有检查通过，继续过滤器链
        filterChain.doFilter(request, response);
    }


    /**
     * 国际化消息处理 - 将错误响应消息转换为对应语言
     *
     * @param request HTTP请求对象
     * @param respModel 响应模型
     * @return 处理后的响应模型
     */
    private RespModel process(HttpServletRequest request, RespModel respModel) {
        // 获取请求中的语言偏好
        String l = request.getHeader("Accept-Language");
        // 使用CommonResultControllerAdvice进行国际化处理
        CommonResultControllerAdvice.process(l, respModel, messageSource);

        return respModel;
    }


}
