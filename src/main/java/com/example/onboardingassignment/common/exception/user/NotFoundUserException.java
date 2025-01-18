package com.example.onboardingassignment.common.exception.user;

import com.example.onboardingassignment.common.exception.GlobalException;

import static com.example.onboardingassignment.common.exception.GlobalExceptionConst.NOT_FOUND_USER;

public class NotFoundUserException extends GlobalException {
    public NotFoundUserException() {
        super(NOT_FOUND_USER);
    }
}
