package com.example.onboardingassignment.security;

import com.example.onboardingassignment.entity.User;
import com.example.onboardingassignment.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
    void 로그인_성공시_RefreshToken_생성() throws ServletException, IOException {
        // given
        String username = "testUser";
        String requestBody = "{\"username\":\"" + username + "\",\"password\":\"password\"}";
        UserRole role = UserRole.ROLE_USER;
        String mockRefreshToken = "mocked.refresh.token";

        // MockServletInputStream 생성
        ServletInputStream inputStream = new MockServletInputStream(requestBody.getBytes());

        given(request.getRequestURI()).willReturn("/sign");
        given(request.getMethod()).willReturn("POST");
        given(request.getInputStream()).willReturn(inputStream);
        given(response.getStatus()).willReturn(200);

        User mockUser = mock(User.class);
        UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);
        given(mockUserDetails.getUser()).willReturn(mockUser);
        given(mockUser.getRole()).willReturn(role);
        given(userDetailsService.loadUserByUsername(username)).willReturn(mockUserDetails);
        given(jwtUtil.createRefreshToken(username, role)).willReturn(mockRefreshToken);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).createRefreshToken(username, role);
        verify(jwtUtil).setRefreshTokenCookie(response, mockRefreshToken);
    }

    @Test
    void 로그인_실패시_RefreshToken을_생성하지_않음() throws ServletException, IOException {
        // given
        String username = "testUser";
        String requestBody = "{\"username\":\"" + username + "\",\"password\":\"wrongPassword\"}";
        ServletInputStream inputStream = new MockServletInputStream(requestBody.getBytes());

        given(request.getRequestURI()).willReturn("/sign");
        given(request.getMethod()).willReturn("POST");
        given(request.getInputStream()).willReturn(inputStream);
        given(response.getStatus()).willReturn(401);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil, never()).createRefreshToken(anyString(), any(UserRole.class));
        verify(jwtUtil, never()).setRefreshTokenCookie(any(), anyString());
    }

    private static class MockServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(byte[] content) {
            this.inputStream = new ByteArrayInputStream(content);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }
    }
}