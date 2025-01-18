package com.example.onboardingassignment.common.exception.user;

import com.example.onboardingassignment.common.exception.GlobalException;

import static com.example.onboardingassignment.common.exception.GlobalExceptionConst.UNAUTHORIZED_OWNERTOKEN;

public class UserRoleException extends GlobalException {
    public UserRoleException () {
        super(UNAUTHORIZED_OWNERTOKEN);
    }
}
