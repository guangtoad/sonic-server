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
package org.cloud.sonic.common.config;

import jakarta.annotation.Resource;
import org.cloud.sonic.common.http.RespModel;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Locale;

/**
 * 统一响应处理增强器 - 实现ResponseBodyAdvice接口，在响应返回前进行国际化消息处理
 *
 * @author JayWenStar, Eason
 * @date 2022/4/11 1:59 上午
 */
@ControllerAdvice
public class CommonResultControllerAdvice implements ResponseBodyAdvice<Object> {
    /** 国际化消息资源管理器 */
    @Resource
    private MessageSource messageSource;

    /**
     * 判断是否支持当前响应类型 - 仅处理JSON格式响应
     *
     * @param returnType 返回类型
     * @param converterType 消息转换器类型
     * @return true表示支持，false表示不支持
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 仅支持Jackson JSON转换器
        return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    /**
     * 在响应体写入前处理 - 对RespModel类型的响应进行国际化消息转换
     *
     * @param body 响应体对象
     * @param returnType 返回类型
     * @param selectedContentType 选中的内容类型
     * @param selectedConverterType 选中的转换器类型
     * @param request 请求对象
     * @param response 响应对象
     * @return 处理后的响应体
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 获取或创建响应容器
        MappingJacksonValue container = getOrCreateContainer(body);

        // 获取请求中的语言偏好
        String l = request.getHeaders().getFirst("Accept-Language");
        // 获取实际的返回体
        Object returnBody = container.getValue();

        // 如果返回体是RespModel类型，进行国际化处理
        if (returnBody instanceof RespModel) {
            RespModel<?> baseResponse = (RespModel) returnBody;
            process(l, baseResponse, messageSource);
        }
        return container;
    }

    /**
     * 国际化消息处理方法 - 将消息key转换为对应语言的消息文本
     *
     * @param l 语言标识（如zh_CN、en_US）
     * @param respModel 响应模型
     * @param messageSource 消息资源管理器
     * @return 处理后的响应模型
     */
    public static RespModel process(String l, RespModel respModel, MessageSource messageSource) {
        // 默认语言为英文
        String language = "en_US";
        if (l != null) {
            language = l;
        }

        // 解析语言标识（格式：语言_地区）
        String[] split = language.split("_");
        Locale locale;
        if (split.length >= 2) {
            locale = new Locale(split[0], split[1]);
        } else {
            locale = new Locale("en", "US");
        }

        // 将消息key转换为对应语言的文本
        respModel.setMessage(messageSource.getMessage(respModel.getMessage(), new Object[]{}, locale));
        return respModel;
    }

    /**
     * 获取或创建MappingJacksonValue容器
     *
     * @param body 原始响应体
     * @return MappingJacksonValue容器
     */
    private MappingJacksonValue getOrCreateContainer(Object body) {
        return (body instanceof MappingJacksonValue ? (MappingJacksonValue) body : new MappingJacksonValue(body));
    }
}
