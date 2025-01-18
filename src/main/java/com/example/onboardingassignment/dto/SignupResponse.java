package com.example.onboardingassignment.dto;

import com.example.onboardingassignment.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String username;
    private String nickname;
    private List<Authority> authorities;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authority {
        private String authorityName;
    }
}
