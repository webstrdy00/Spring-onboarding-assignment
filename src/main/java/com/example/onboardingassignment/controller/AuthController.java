package com.example.onboardingassignment.controller;

import com.example.onboardingassignment.common.dto.ApiResponse;
import com.example.onboardingassignment.dto.SignRequestDto;
import com.example.onboardingassignment.dto.SignResponse;
import com.example.onboardingassignment.dto.SignupRequestDto;
import com.example.onboardingassignment.dto.SignupResponse;
import com.example.onboardingassignment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(signupRequestDto)));
    }

    // 로그인
    @PostMapping("/sign")
    public ResponseEntity<ApiResponse<SignResponse>> sign(@Valid @RequestBody SignRequestDto signRequestDto){
        return ResponseEntity.ok(ApiResponse.success(authService.sign(signRequestDto)));
    }
}
