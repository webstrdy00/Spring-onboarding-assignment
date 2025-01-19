package com.example.onboardingassignment.security;

import com.example.onboardingassignment.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        String path = req.getRequestURI();
        // 로그인, 회원가입 요청은 이 필터를 건너뜁니다.
        if (path.startsWith("/swagger-ui") || path.startsWith("/api-docs") || path.equals("/signup") || path.equals("/sign") || path.equals("/actuator/health") || path.equals("/") || path.equals("/error") || path.equals("/favicon.ico")){
            filterChain.doFilter(req, res);
            return;
        }

        // 로그아웃 요청은 인증 검사만 하고 사용자 정보 조회는 하지 않음
        if (path.equals("/signout")) {
            log.info("JwtAuthenticationFilter 시작: {}", req.getRequestURI());
            String accessToken = jwtUtil.getJwtFromHeader(req);
            if (StringUtils.hasText(accessToken) && jwtUtil.validateToken(accessToken)) {
                filterChain.doFilter(req, res);
                return;
            }
        }

        try {
            String accessToken = jwtUtil.getJwtFromHeader(req);
            log.info("Extracted accessToken from header: {}", accessToken);
            Optional<String> refreshTokenOpt = jwtUtil.getRefreshTokenFromCooke(req);

            if (StringUtils.hasText(accessToken)) {
                if (jwtUtil.validateToken(accessToken)) {
                    setAuthentication(jwtUtil.getUserInfoFromToken(accessToken).getSubject());
                } else if (refreshTokenOpt.isPresent() && jwtUtil.validateRefreshToken(refreshTokenOpt.get())) {
                    // AccessToken이 만료되었지만 RefreshToken이 유효한 경우
                    String refreshToken = refreshTokenOpt.get();
                    String userName = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();

                    // Redis에서 RefreshToken 조회
                    Optional<String> storedRefreshToken = jwtUtil.getRefreshTokenFromRedis(userName);

                    if (storedRefreshToken.isPresent() && storedRefreshToken.get().equals(refreshToken)) {
                        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(userName);
                        UserRole role = userDetails.getUser().getRole();

                        String newAccessToken = jwtUtil.createAccessToken(userName, role);
                        res.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + newAccessToken);

                        String newRefreshToken = jwtUtil.updateRefreshToken(userName, role);
                        jwtUtil.setRefreshTokenCookie(res, newRefreshToken);

                        setAuthentication(userName);
                    }
                }
                filterChain.doFilter(req, res);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // 인증 처리
    public void setAuthentication(String userName) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(userName);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String userName) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
