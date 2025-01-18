package com.example.onboardingassignment.security;

import com.example.onboardingassignment.entity.User;
import com.example.onboardingassignment.enums.UserRole;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
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

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Test
    void 로그인_요청이_아닌_경우_필터를_건너뛴다() throws ServletException, IOException {
        // given
        given(request.getRequestURI()).willReturn("/api/test");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("로그인 성공시 RefreshToken 생성")
    void createRefreshTokenOnLoginSuccess() throws ServletException, IOException {
        // given
        String username = "testUser";
        String requestBody = "{\"username\":\"" + username + "\",\"password\":\"password\"}";
        UserRole role = UserRole.ROLE_USER;
        String mockRefreshToken = "mocked.refresh.token";

        given(request.getRequestURI()).willReturn("/sign");
        given(request.getMethod()).willReturn("POST");
        given(request.getReader()).willReturn(new BufferedReader(new StringReader(requestBody)));
        given(response.isCommitted()).willReturn(false);
        given(response.getStatus()).willReturn(200);

        User mockUser = mock(User.class);
        UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);
        given(mockUserDetails.getUser()).willReturn(mockUser);
        given(mockUser.getRole()).willReturn(role);
        given(userDetailsService.loadUserByUsername(username)).willReturn(mockUserDetails);

        // createRefreshToken에 대한 mock 동작
        given(jwtUtil.createRefreshToken(username, role)).willReturn(mockRefreshToken);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).createRefreshToken(username, role);
        verify(jwtUtil).setRefreshTokenCookie(response, mockRefreshToken);
    }

    @Test
    @DisplayName("로그인 실패시 RefreshToken을 생성하지 않음")
    void doNotCreateRefreshTokenOnLoginFailure() throws ServletException, IOException {
        // given
        given(request.getRequestURI()).willReturn("/sign");
        given(request.getMethod()).willReturn("POST");
        given(response.isCommitted()).willReturn(false);
        given(response.getStatus()).willReturn(401);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verifyNoInteractions(jwtUtil);
    }
}