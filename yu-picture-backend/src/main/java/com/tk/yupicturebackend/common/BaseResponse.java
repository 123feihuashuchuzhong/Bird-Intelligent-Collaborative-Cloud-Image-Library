package com.tk.yupicturebackend.common;

import com.tk.yupicturebackend.exception.ErrorCode;

import java.io.Serializable;
import lombok.Data;
//全局响应封装
@Data
public class BaseResponse <T> implements Serializable {
    private int code;
    private String message;
    private T data;
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public BaseResponse(int code, T data) {
        this(code, data, " ");
    }
public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage());
    }


}

