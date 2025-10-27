package com.sosd.sosd_backend.exception.auth;

import com.sosd.sosd_backend.exception.CustomException;
import com.sosd.sosd_backend.exception.ErrorCode;

public class SignUpDuplicateUserException extends CustomException {
    public SignUpDuplicateUserException(){
        super(ErrorCode.USER_ALREADY_EXISTS);
    }
}
