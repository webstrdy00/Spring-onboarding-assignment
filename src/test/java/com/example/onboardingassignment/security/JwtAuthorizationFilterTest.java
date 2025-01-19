package com.example.onboardingassignment.security;

import com.example.onboardingassignment.entity.User;
import com.example.onboardingassignment.enums.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationFilterTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("허용된 경로는 필터를 건너뛴다")
    void skipFilterForAllowedPaths() throws ServletException, IOException {
        // given
        given(request.getRequestURI()).willReturn("/sign");

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(jwtUtil);
    }

    @Test
    @DisplayName("유효한 AccessToken으로 인증 성공")
    void authenticateWithValidAccessToken() throws ServletException, IOException {
        // given
        String token = "valid.access.token";
        given(request.getRequestURI()).willReturn("/api/test");
        given(jwtUtil.getJwtFromHeader(request)).willReturn(token);
        given(jwtUtil.validateToken(token)).willReturn(true);

        Claims mockClaims = mock(Claims.class);
        given(mockClaims.getSubject()).willReturn("testUser");
        given(jwtUtil.getUserInfoFromToken(token)).willReturn(mockClaims);

        UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(mockUserDetails);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void 만료된_Access_Token과_유효한_Refresh_Token으로_토큰_갱신() throws ServletException, IOException {
        // given
        String accessToken = "expired.access.token";
        String refreshToken = "valid.refresh.token";
        String username = "testUser";

        given(request.getRequestURI()).willReturn("/api/test");
        given(jwtUtil.getJwtFromHeader(request)).willReturn(accessToken);
        given(jwtUtil.validateToken(accessToken)).willReturn(false);

        given(jwtUtil.getRefreshTokenFromCooke(request))
                .willReturn(Optional.of(refreshToken));
        given(jwtUtil.validateRefreshToken(refreshToken)).willReturn(true);

        Claims mockClaims = mock(Claims.class);
        given(mockClaims.getSubject()).willReturn(username);
        given(jwtUtil.getUserInfoFromToken(refreshToken)).willReturn(mockClaims);

        given(jwtUtil.getRefreshTokenFromRedis(username))
                .willReturn(Optional.of(refreshToken));

        UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);
        User mockUser = mock(User.class);
        given(mockUserDetails.getUser()).willReturn(mockUser);
        given(mockUser.getRole()).willReturn(UserRole.ROLE_USER);
        given(userDetailsService.loadUserByUsername(username))
                .willReturn(mockUserDetails);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).createAccessToken(eq(username), any(UserRole.class));
        verify(jwtUtil).updateRefreshToken(eq(username), any(UserRole.class));
        verify(filterChain).doFilter(request, response);
    }
}