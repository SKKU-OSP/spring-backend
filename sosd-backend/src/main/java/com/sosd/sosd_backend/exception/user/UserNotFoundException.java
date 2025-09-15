package com.sosd.sosd_backend.exception.user;

import com.sosd.sosd_backend.exception.CustomException;
import com.sosd.sosd_backend.exception.ErrorCode;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
