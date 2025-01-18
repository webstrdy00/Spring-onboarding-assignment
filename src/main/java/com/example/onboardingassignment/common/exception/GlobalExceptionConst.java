package com.example.onboardingassignment.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalExceptionConst {
    // 상태코드 400
    DUPLICATE_PASSWORD(HttpStatus.BAD_REQUEST, " 새 비밀번호는 이전에 사용한 비밀번호와 같을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, " 새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다."),

    UNAUTHORIZED_PASSWORD(HttpStatus.UNAUTHORIZED, " 비밀번호를 확인해주세요."),
    UNAUTHORIZED_OWNERTOKEN(HttpStatus.UNAUTHORIZED, " 유저 토큰이 틀렸습니다."),
    UNAUTHORIZED_ADMIN(HttpStatus.UNAUTHORIZED, " 관리자 권한이 존재하지 않습니다."),

    // 상태코드 403
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, " 회원이 존재하지 않습니다."),
    NOT_FOUND_USERNAME(HttpStatus.NOT_FOUND, " 유저 네임을 확인해주세요."),
    DELETED_USER(HttpStatus.NOT_FOUND, " 탈퇴된 회원입니다."),

    // 상태코드 409
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, " 중복된 유저 네임입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
