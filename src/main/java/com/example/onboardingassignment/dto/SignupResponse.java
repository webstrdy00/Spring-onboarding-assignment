package com.example.onboardingassignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Schema(description = "회원가입 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    @Schema(description = "사용자 이름", example = "user123")
    private String username;
    @Schema(description = "사용자 닉네임", example = "Mentos")
    private String nickname;
    @Schema(description = "사용자 권한 목록")
    private List<Authority> authorities;

    @Schema(description = "권한 정보")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authority {
        @Schema(description = "권한 이름", example = "ROLE_USER")
        private String authorityName;
    }
}
