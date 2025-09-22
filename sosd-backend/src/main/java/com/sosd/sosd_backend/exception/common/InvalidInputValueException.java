package com.sosd.sosd_backend.exception.common;

import com.sosd.sosd_backend.exception.CustomException;
import com.sosd.sosd_backend.exception.ErrorCode;

public class InvalidInputValueException extends CustomException {
    public InvalidInputValueException(){
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}
