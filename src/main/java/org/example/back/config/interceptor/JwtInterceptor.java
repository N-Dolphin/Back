package org.example.back.config.interceptor;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.exception.InvalidTokenException;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof SimpMessageHeaderAccessor) {

            if (HttpMethod.OPTIONS.matches(request.getMethod())) {
                return true;
            }
            // // OPTIONS 요청은 통과
            // if (request.getMethod().equals("OPTIONS")) {
            //     return true;
            // }

            // WebSocket 연결 처리
            SimpMessageHeaderAccessor headerAccessor = (SimpMessageHeaderAccessor) handler;
            String token = headerAccessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            token = token.substring(7);

            try {
                String userId = jwtTokenProvider.extractSubject(token);
                headerAccessor.getSessionAttributes().put("profileId", Long.valueOf(userId));
            } catch (ExpiredJwtException e) {
                throw new InvalidTokenException("토큰이 만료되었습니다. 다시 로그인하세요.");
            } catch (Exception e) {
                throw new InvalidTokenException("토큰이 유효하지 않습니다. 다시 로그인하세요.");
            }
        } else {
            // HTTP 요청 처리
            String token = request.getHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            token = token.substring(7);

            try {
                String userId = jwtTokenProvider.extractSubject(token);
                request.setAttribute("userId", userId);
            } catch (ExpiredJwtException e) {
                throw new InvalidTokenException("토큰이 만료되었습니다. 다시 로그인하세요.");
            } catch (Exception e) {
                throw new InvalidTokenException("토큰이 유효하지 않습니다. 다시 로그인하세요.");
            }
        }

        return true;
    }
}