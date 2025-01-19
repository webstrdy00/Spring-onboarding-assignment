package com.example.onboardingassignment.security;

import com.example.onboardingassignment.common.dto.ApiResponse;
import com.example.onboardingassignment.common.dto.ErrorResponse;
import com.example.onboardingassignment.common.exception.GlobalException;
import com.example.onboardingassignment.common.exception.GlobalExceptionConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (GlobalException e) {
            setErrorResponse(response, e);
        } catch (RuntimeException e) {
            setErrorResponse(response, new GlobalException(GlobalExceptionConst.UNAUTHORIZED_OWNERTOKEN));
        }
    }

    private void setErrorResponse(HttpServletResponse response, GlobalException e) {
        response.setStatus(e.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        try {
            ErrorResponse errorResponse = new ErrorResponse(
                    e.getHttpStatus().value(),
                    e.getMessage()
            );

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(
                    ApiResponse.error(errorResponse)
            );

            response.getWriter().write(jsonResponse);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
