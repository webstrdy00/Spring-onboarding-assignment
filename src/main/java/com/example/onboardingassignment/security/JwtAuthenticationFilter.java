package com.example.onboardingassignment.security;

import com.example.onboardingassignment.enums.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // /sign 경로의 POST 요청에만 RefreshToken 처리를 수행
        if (!request.getRequestURI().equals("/sign") || !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 다음 필터 실행 (AuthService.sign() 메서드가 실행됨)
        filterChain.doFilter(request, response);

        if (!response.isCommitted() && response.getStatus() == 200) {
            String username = request.getReader().lines().collect(Collectors.joining());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(username);
            String userName = node.get("username").asText();

            // UserDetails를 통해 실제 사용자 정보 조회
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(userName);
            UserRole userRole = userDetails.getUser().getRole();

            // 실제 사용자의 역할로 RefreshToken 생성
            String refreshToken = jwtUtil.createRefreshToken(userName, userRole);
            jwtUtil.setRefreshTokenCookie(response, refreshToken);
        }
    }
}
