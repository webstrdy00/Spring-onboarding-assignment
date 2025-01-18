package com.example.onboardingassignment.common.exception.user;

import com.example.onboardingassignment.common.exception.GlobalException;

import static com.example.onboardingassignment.common.exception.GlobalExceptionConst.DELETED_USER;

public class DeletedUserException extends GlobalException {
    public DeletedUserException() {
        super(DELETED_USER);
    }
}
