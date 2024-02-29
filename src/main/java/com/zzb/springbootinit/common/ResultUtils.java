package com.zzb.springbootinit.common;

/**
 * 返回工具类
 *
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 *
 * 主要用于简化BaseResponse的操作，将成功，失败的一些通用情况进行的静态方法的封装，然后可以很方便的进行调用，比如调用
 * success方法，响应状态码就是 0，然后会将 data封装到 BaseResponse的 data属性，message为"ok"。
 * 如果你的前端想要响应状态吗为200，那么将这里的0改成200就可以了，这边的message为固定消息"ok"。
 * 可以再设置一个静态方法，进行方法重构，success形参为（T data,String message),然后可以动态定义成功的时候的返回消息。
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
