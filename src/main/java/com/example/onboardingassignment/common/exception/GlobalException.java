package com.example.onboardingassignment.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class GlobalException extends RuntimeException{
    private final HttpStatus httpStatus;

    // 상수 -> 상태 코드랑 메시지가 담겨있다.e
    public GlobalException(GlobalExceptionConst globalExceptionConst) {
        super(globalExceptionConst.getHttpStatus() + " " + globalExceptionConst.name() + globalExceptionConst.getMessage());
        this.httpStatus = globalExceptionConst.getHttpStatus();
    }
}
