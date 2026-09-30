package com.walkmission.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;

    public JwtAuthInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        
        String token = header.substring(7);
        try {
            var claims = jwtProvider.getClaims(token);
            if (!JwtProvider.ACCESS_TOKEN_TYPE.equals(claims.get(JwtProvider.TOKEN_TYPE_CLAIM, String.class))) {
                throw new IllegalArgumentException("Not an access token");
            }
            request.setAttribute("userId", Long.parseLong(claims.getSubject()));
            request.setAttribute("userUuid", claims.get("userUuid", String.class));
            return true;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}
