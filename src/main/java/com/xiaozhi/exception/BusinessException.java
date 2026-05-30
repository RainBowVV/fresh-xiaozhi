package com.xiaozhi.exception;

import com.xiaozhi.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
