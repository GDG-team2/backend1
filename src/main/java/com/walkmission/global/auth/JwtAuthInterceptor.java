package com.walkmission.global.auth;

import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = header.substring(7);
        Claims claims;
        try {
            claims = jwtProvider.getClaims(token);
        } catch (ExpiredJwtException e) {
            // 프론트는 이 코드를 받으면 /auth/reissue를 호출한다.
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!JwtProvider.ACCESS_TOKEN_TYPE.equals(claims.get(JwtProvider.TOKEN_TYPE_CLAIM, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        request.setAttribute("userId", Long.parseLong(claims.getSubject()));
        request.setAttribute("userUuid", claims.get("userUuid", String.class));
        return true;
    }
}
