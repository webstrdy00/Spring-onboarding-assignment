package com.example.onboardingassignment.common.exception.user;

import com.example.onboardingassignment.common.exception.GlobalException;

import static com.example.onboardingassignment.common.exception.GlobalExceptionConst.DUPLICATE_USERNAME;

public class DuplicateUserNameException extends GlobalException {

    public DuplicateUserNameException() {
        super(DUPLICATE_USERNAME);
    }
}
