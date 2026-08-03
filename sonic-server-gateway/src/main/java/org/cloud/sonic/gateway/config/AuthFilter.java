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
package org.cloud.sonic.gateway.config;

import com.alibaba.fastjson.JSONObject;
import org.cloud.sonic.common.http.RespEnum;
import org.cloud.sonic.common.http.RespModel;
import org.cloud.sonic.common.tools.JWTTokenTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关认证过滤器 - 实现GlobalFilter接口，在Gateway层统一验证JWT Token
 *
 * 过滤逻辑：
 * 1. 检查请求路径是否在白名单中，白名单请求直接放行
 * 2. 检查请求头中是否携带SonicToken
 * 3. 验证Token是否有效
 * 4. 验证失败返回未授权响应
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    /** 白名单路径列表，通过配置项 filter.white-list 配置 */
    @Value("${filter.white-list}")
    private List<String> whiteList;

    /** JWT Token工具类，用于验证Token有效性 */
    @Autowired
    private JWTTokenTool jwtTokenTool;

    /**
     * 网关过滤核心方法 - 执行Token认证逻辑
     *
     * @param exchange 服务器Web交换对象
     * @param chain 网关过滤器链
     * @return Mono<Void> 异步响应
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 检查请求路径是否在白名单中，白名单请求直接放行
        for (String white : whiteList) {
            if (exchange.getRequest().getURI().getPath().contains(white)) {
                return chain.filter(exchange);
            }
        }

        // 获取请求头中的Token
        String token = exchange.getRequest().getHeaders().getFirst("SonicToken");

        // 获取响应对象并设置响应头
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");

        // 创建未授权响应
        DataBuffer buffer = sendResp(response);

        // 2. Token为空，返回未授权
        if (token == null) {
            return response.writeWith(Mono.just(buffer));
        }

        // 3. 验证Token有效性
        if (!jwtTokenTool.verify(token)) {
            return response.writeWith(Mono.just(buffer));
        }

        // 4. 验证通过，继续过滤器链
        return chain.filter(exchange);
    }

    /**
     * 获取过滤器执行顺序 - 值越小优先级越高
     *
     * @return 过滤器顺序，-100表示高优先级
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 创建未授权响应数据
     *
     * @param response 服务器HTTP响应对象
     * @return 包含未授权错误信息的DataBuffer
     */
    private DataBuffer sendResp(ServerHttpResponse response) {
        // 构建未授权响应模型
        JSONObject result = (JSONObject) JSONObject.toJSON(new RespModel(RespEnum.UNAUTHORIZED));
        // 将JSON转换为DataBuffer
        DataBuffer buffer = response.bufferFactory().wrap(result.toJSONString().getBytes(StandardCharsets.UTF_8));
        return buffer;
    }
}