package com.example.onboardingassignment.common.exception.user;

import com.example.onboardingassignment.common.exception.GlobalException;

import static com.example.onboardingassignment.common.exception.GlobalExceptionConst.INVALID_PASSWORD;

public class InvalidPasswordFormatException extends GlobalException {
    public InvalidPasswordFormatException() {
        super(INVALID_PASSWORD);
    }
}
