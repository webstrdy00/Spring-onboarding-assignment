package com.example.onboardingassignment.common.exception.user;

import com.example.onboardingassignment.common.exception.GlobalException;

import static com.example.onboardingassignment.common.exception.GlobalExceptionConst.UNAUTHORIZED_PASSWORD;

public class UnauthorizedPasswordException extends GlobalException {
    public UnauthorizedPasswordException() {
        super(UNAUTHORIZED_PASSWORD);
    }
}
