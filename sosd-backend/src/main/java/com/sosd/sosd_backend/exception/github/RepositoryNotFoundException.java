package com.sosd.sosd_backend.exception.github;

import com.sosd.sosd_backend.exception.CustomException;
import com.sosd.sosd_backend.exception.ErrorCode;

public class RepositoryNotFoundException extends CustomException {
    public RepositoryNotFoundException() {
        super(ErrorCode.REPOSITORY_NOT_FOUND);
    }
}
