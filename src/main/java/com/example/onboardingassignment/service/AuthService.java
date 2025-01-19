package com.example.onboardingassignment.service;

import com.example.onboardingassignment.common.exception.user.*;
import com.example.onboardingassignment.dto.SignRequestDto;
import com.example.onboardingassignment.dto.SignResponse;
import com.example.onboardingassignment.dto.SignupRequestDto;
import com.example.onboardingassignment.dto.SignupResponse;
import com.example.onboardingassignment.entity.User;
import com.example.onboardingassignment.enums.UserRole;
import com.example.onboardingassignment.enums.UserStatus;
import com.example.onboardingassignment.repository.UserRepository;
import com.example.onboardingassignment.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${owner.token}")
    private String OWNER_TOKEN;

    @Transactional
    public SignupResponse signup(SignupRequestDto signupRequestDto) {
        // 사용자 이름 중복 체크
        if (userRepository.existsByUserName(signupRequestDto.getUsername())) {
            throw new DuplicateUserNameException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // 사용자 ROLE 확인
        UserRole role = UserRole.ROLE_USER;
        if (signupRequestDto.isOwner()) {
            if (!OWNER_TOKEN.equals(signupRequestDto.getOwnerToken())) {
                throw new UserRoleException();
            }
            role = UserRole.ROLE_ADMIN;
        }

        // 새로운 사용자 생성
        User newUser = new User(
                signupRequestDto.getUsername(),
                encodedPassword,
                signupRequestDto.getNickname(),
                role
        );

        userRepository.save(newUser);

        // 응답 생성
        return SignupResponse.builder()
                .username(newUser.getUserName())
                .nickname(newUser.getNickName())
                .authorities(Collections.singletonList(
                        new SignupResponse.Authority(newUser.getRole().getAuthority())))
                .build();
    }

    @Transactional
    public SignResponse sign(SignRequestDto signRequestDto){
        // 사용자 찾기
        User user = userRepository.findByUserName(signRequestDto.getUsername())
                .orElseThrow(NotFoundUserException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(signRequestDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedPasswordException();
        }

        // UserStatus 가 DELETED 면 로그인 불가능
        if (user.getStatus().equals(UserStatus.DELETED)) {
            throw new DeletedUserException();
        }

        // Access Token 생성
        String accessToken = jwtUtil.createAccessToken(user.getUserName(), user.getRole());

        return SignResponse.builder()
                .token(accessToken)
                .build();
    }
}
