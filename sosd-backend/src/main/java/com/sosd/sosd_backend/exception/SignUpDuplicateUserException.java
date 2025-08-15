package com.sosd.sosd_backend.exception;

public class SignUpDuplicateUserException extends CustomException{
    public SignUpDuplicateUserException(){
        super(ErrorCode.USER_ALREADY_EXISTS);
    }
}
