package com.zzb.springbootinit.common;

/**
 * 自定义错误码
 *
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 *
 * ErrorCode 配合上面的 ResultUtils使用，可以定义枚举类将常规的响应状态码和响应信息进行封装。
 * 比如：无权限访问(40300)，服务器内部异常(50000)，你可以添加自定义的一些专属你自己项目的响应状态码，
 * 例如：
 * API项目接口调用失败，可以是INTERFACE ERROR(50003,"接口调用失败")。
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
