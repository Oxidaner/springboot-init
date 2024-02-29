package com.zzb.springbootinit.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用返回类
 *
 * @param <T>
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 * 
 */
@Data
public class BaseResponse<T> implements Serializable {

    //表示响应状态码
    private int code;

    //表示响应数据
    private T data;

    //表示成功或失败额外消息
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
