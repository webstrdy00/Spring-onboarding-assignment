package com.example.onboardingassignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Schema(description = "로그인 요청 DTO")
@Getter
@NoArgsConstructor
public class SignRequestDto {
    @Schema(description = "사용자 이름", example = "user123")
    @NotBlank
    private String username;

    @Schema(description = "비밀번호", example = "Password123!")
    @NotBlank
    private String password;
}
