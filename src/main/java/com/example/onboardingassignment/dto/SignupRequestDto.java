package com.example.onboardingassignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {
    @Schema(description = "사용자 이름", example = "user123")
    @NotBlank
    private String username;

//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\p{Punct}])[A-Za-z\\d\\p{Punct}]{8,20}$",
//            message = "비밀번호는 최소 8자리 이상, 숫자, 영문, 특수문자를 1자 이상 포함되어야 합니다.")
    @Schema(description = "비밀번호", example = "Password123!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @Schema(description = "닉네임", example = "Mentos")
    @NotBlank
    private String nickname;

    private boolean owner = false;

    private String ownerToken = "";
}
