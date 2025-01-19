package com.example.onboardingassignment.controller;

import com.example.onboardingassignment.common.dto.ApiResponse;
import com.example.onboardingassignment.common.dto.ErrorResponse;
import com.example.onboardingassignment.dto.SignRequestDto;
import com.example.onboardingassignment.dto.SignResponse;
import com.example.onboardingassignment.dto.SignupRequestDto;
import com.example.onboardingassignment.dto.SignupResponse;
import com.example.onboardingassignment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @Operation(
            summary = "회원가입",
            description = "신규 사용자를 등록합니다. 비밀번호는 8자 이상이어야 하며, 영문/숫자/특수문자를 포함해야 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignupResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복된 사용자 이름",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(signupRequestDto)));
    }

    // 로그인
    @Operation(
            summary = "로그인",
            description = "사용자 인증 후 JWT 토큰을 발급합니다. 발급된 토큰은 이후 요청의 Authorization 헤더에 'Bearer {token}' 형식으로 포함되어야 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (잘못된 비밀번호)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/sign")
    public ResponseEntity<ApiResponse<SignResponse>> sign(@Valid @RequestBody SignRequestDto signRequestDto){
        return ResponseEntity.ok(ApiResponse.success(authService.sign(signRequestDto)));
    }

    // 로그아웃
    @Operation(
            summary = "로그아웃",
            description = "사용자 로그아웃을 처리합니다. 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<String>> signout(){
        return ResponseEntity.ok(ApiResponse.success(authService.signout()));
    }
}
