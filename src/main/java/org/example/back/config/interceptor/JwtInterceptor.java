package org.example.back.config.interceptor;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.exception.InvalidTokenException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (HttpMethod.OPTIONS.matches(request.getMethod())){
            return true;
        }

        String token = request.getHeader("Authorization");

        // "Bearer " 문자열로 시작하는지 확인하고, 실제 토큰 값만 추출
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);

        try {
            // JWT 유효성 검증 및 subject 추출
            String userId = jwtTokenProvider.extractSubject(token);

            // 사용자 ID를 request에 저장하여 이후 요청에서 사용 가능하도록 설정
            request.setAttribute("userId", userId);

        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("토큰이 만료되었습니다. 다시 로그인하세요.");
        } catch (Exception e) {
            throw new InvalidTokenException("토큰이 유효하지 않습니다. 다시 로그인하세요.");
        }

        return true; // 검증 성공 시 컨트롤러로 요청을 넘김
    }
}
