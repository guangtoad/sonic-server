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

import jakarta.validation.ConstraintViolationException;
import org.cloud.sonic.common.exception.SonicException;
import org.cloud.sonic.common.http.RespEnum;
import org.cloud.sonic.common.http.RespModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 - 统一拦截并处理应用中的所有异常，返回标准化的错误响应
 *
 * @author ZhouYiXun
 * @date 2021/8/15 18:26
 */
@RestControllerAdvice
@Order(1)
public class GlobalWebException {
    /** 日志记录器 */
    private final Logger logger = LoggerFactory.getLogger(GlobalWebException.class);

    /**
     * 全局异常处理方法 - 捕获所有未处理的异常并转换为统一的响应格式
     *
     * @param exception 捕获到的异常对象
     * @return 统一的错误响应模型
     */
    @ExceptionHandler(Exception.class)
    public RespModel ErrHandler(Exception exception) {
        // 记录异常日志
        logger.error(exception.getMessage());

        // 根据异常类型返回对应的错误状态码
        if (exception instanceof MissingServletRequestParameterException) {
            // 请求参数缺失
            return new RespModel(RespEnum.PARAMS_MISSING_ERROR);
        } else if (exception instanceof ConstraintViolationException) {
            // 参数约束验证失败
            return new RespModel(RespEnum.PARAMS_VIOLATE_ERROR);
        } else if (exception instanceof MethodArgumentNotValidException) {
            // @Validated 参数校验失败
            return new RespModel(RespEnum.PARAMS_NOT_VALID);
        } else if (exception instanceof HttpMessageNotReadableException) {
            // 请求体解析失败（JSON格式错误等）
            return new RespModel(RespEnum.PARAMS_NOT_READABLE);
        } else if (exception instanceof SonicException) {
            // 业务自定义异常，直接返回异常消息
            return new RespModel(4006, exception.getMessage());
        } else {
            // 未知异常，打印完整堆栈并返回兜底错误
            exception.printStackTrace();
            return new RespModel(RespEnum.UNKNOWN_ERROR);
        }
    }
}
