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
package org.cloud.sonic.common.http;

/**
 * 接口响应状态码枚举 - 定义系统统一的响应状态码和国际化消息key
 *
 * @author ZhouYiXun
 * @date 2021/8/15 18:26
 */
public enum RespEnum {
    /** 操作成功 - 通用成功状态码 */
    HANDLE_OK(2000, "ok.handle"),
    /** 查询成功 */
    SEARCH_OK(2000, "ok.search"),
    /** 更新成功 */
    UPDATE_OK(2000, "ok.update"),
    /** 删除成功 */
    DELETE_OK(2000, "ok.delete"),
    /** 上传成功 */
    UPLOAD_OK(2000, "ok.upload"),
    /** 发送成功 */
    SEND_OK(2000, "ok.send"),
    /** 复制成功 */
    COPY_OK(2000, "ok.copy"),
    /** 查询失败 */
    SEARCH_FAIL(3005, "fail.search"),
    /** 更新失败 */
    UPDATE_FAIL(3004, "fail.update"),
    /** 删除失败 */
    DELETE_FAIL(3002, "fail.delete"),
    /** 上传失败 */
    UPLOAD_FAIL(3003, "fail.upload"),
    /** 未授权 - Token无效或未携带 */
    UNAUTHORIZED(1001, "unauthorized"),
    /** 服务未找到 - Eureka服务发现失败 */
    SERVICE_NOT_FOUND(1002, "not.found.service"),
    /** 权限拒绝 - 用户无访问权限 */
    PERMISSION_DENIED(1003, "permission.denied"),
    /** 资源未找到 - 请求的资源不存在 */
    RESOURCE_NOT_FOUND(1004, "not.found.resource"),
    /** ID未找到 - 根据ID未查询到记录 */
    ID_NOT_FOUND(3001, "not.found.id"),
    /** 设备未找到 */
    DEVICE_NOT_FOUND(3002, "not.found.device"),
    /** Agent离线 - 设备代理未在线 */
    AGENT_NOT_ONLINE(5001, "not.online.agent"),
    /** 参数校验失败 - @Validated校验未通过 */
    PARAMS_NOT_VALID(4004, "not.valid.params"),
    /** 参数不可读 - 请求体解析失败 */
    PARAMS_NOT_READABLE(4005, "not.readable.params"),
    /** 参数缺失 - 必需的请求参数缺失 */
    PARAMS_MISSING_ERROR(4001, "error.params.missing"),
    /** 参数违反约束 - 约束验证失败 */
    PARAMS_VIOLATE_ERROR(4002, "error.params.violate"),
    /** 未知错误 - 兜底错误处理 */
    UNKNOWN_ERROR(4003, "error.unknown");

    private int code;
    private String message;

    RespEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
