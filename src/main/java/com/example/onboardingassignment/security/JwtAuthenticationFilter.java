package com.example.onboardingassignment.security;

import com.example.onboardingassignment.config.CachedBodyHttpServletRequest;
import com.example.onboardingassignment.enums.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // /sign 경로의 POST 요청에만 RefreshToken 처리를 수행
        if (!request.getRequestURI().equals("/sign") || !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("JwtAuthenticationFilter 시작: {}", request.getRequestURI());

        String requestData = new String(request.getInputStream().readAllBytes());
        log.info("Request body: {}", requestData);

        // JSON 파싱
        JsonNode node = objectMapper.readTree(requestData);
        String userName = node.get("username").asText();

        // Request를 래핑
        HttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, requestData.getBytes());

        // 사용자 검증
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(userName);

            // 응답 래퍼 생성
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            // 다음 필터 실행
            filterChain.doFilter(wrappedRequest, responseWrapper);

            // 응답 상태 확인
            if (responseWrapper.getStatus() == 200) {
                log.info("로그인 성공. RefreshToken 생성 시작. Username: {}", userName);

                UserRole userRole = userDetails.getUser().getRole();
                String refreshToken = jwtUtil.createRefreshToken(userName, userRole);
                jwtUtil.setRefreshTokenCookie(response, refreshToken);

                log.info("RefreshToken 처리 완료. Username: {}", userName);
            }

            // 응답 내용 복사
            responseWrapper.copyBodyToResponse();

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            filterChain.doFilter(wrappedRequest, response);
        }
    }
}
