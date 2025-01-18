package com.example.onboardingassignment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignRequestDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
