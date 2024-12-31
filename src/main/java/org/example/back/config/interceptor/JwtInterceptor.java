package org.example.back.config.interceptor;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.exception.InvalidTokenException;
import org.example.back.user.exception.TokenExpiredException;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class JwtInterceptor implements HandlerInterceptor {
//
//     private final JwtTokenProvider jwtTokenProvider;
//
//     @Override
//     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//         log.info("JwtInterceptor - Request Path: {}", request.getRequestURI());
//         log.info("JwtInterceptor - Request Method: {}", request.getMethod());
//
//         if (HttpMethod.OPTIONS.matches(request.getMethod())) {
//             return true;
//         }
//
//         if (handler instanceof SimpMessageHeaderAccessor) {
//             // WebSocket 연결 처리
//             SimpMessageHeaderAccessor headerAccessor = (SimpMessageHeaderAccessor) handler;
//             String token = headerAccessor.getFirstNativeHeader("Authorization");
//
//             if (token == null || !token.startsWith("Bearer ")) {
//                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                 return false;
//             }
//
//             token = token.substring(7);
//
//             try {
//                 String userId = jwtTokenProvider.extractSubject(token);
//                 request.setAttribute("userId", userId);
//             } catch (TokenExpiredException e) {
//                 throw e;
//             } catch (Exception e) {
//                 throw new InvalidTokenException("토큰이 유효하지 않습니다. 다시 로그인하세요.");
//             }
//         } else {
//             // HTTP 요청 처리
//             String token = request.getHeader("Authorization");
//
//             if (token == null || !token.startsWith("Bearer ")) {
//                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                 return false;
//             }
//
//             token = token.substring(7);
//
//             try {
//                 String userId = jwtTokenProvider.extractSubject(token);
//                 request.setAttribute("userId", userId);
//             } catch (ExpiredJwtException e) {
//                 throw new TokenExpiredException();
//             }  catch (Exception e) {
//                 throw new InvalidTokenException("토큰이 유효하지 않습니다. 다시 로그인하세요.");
//             }
//         }
//
//         return true;
//     }
// }
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_ID_ATTRIBUTE = "userId";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("JwtInterceptor - Request Path: {}", request.getRequestURI());
        log.info("JwtInterceptor - Request Method: {}", request.getMethod());

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (handler instanceof SimpMessageHeaderAccessor headerAccessor) {
            return handleWebSocketRequest(headerAccessor, request, response);
        }

        return handleHttpRequest(request, response);
    }

    private boolean handleWebSocketRequest(SimpMessageHeaderAccessor headerAccessor,
        HttpServletRequest request,
        HttpServletResponse response) {
        String token = headerAccessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        return processToken(token, request, response);
    }

    private boolean handleHttpRequest(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        return processToken(token, request, response);
    }

    private boolean processToken(String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            String validToken = validateAndExtractToken(token);
            String userId = jwtTokenProvider.extractSubject(validToken);
            request.setAttribute(USER_ID_ATTRIBUTE, userId);
            return true;
        } catch (Exception e) {
            handleTokenException(e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private String validateAndExtractToken(String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("토큰이 존재하지 않거나 Bearer 형식이 아닙니다.");
        }
        return token.substring(BEARER_PREFIX.length());
    }

    private void handleTokenException(Exception e) {
        if (e instanceof ExpiredJwtException) {
            throw new TokenExpiredException();
        } else if (e instanceof InvalidTokenException) {
            throw (InvalidTokenException) e;
        }
        throw new InvalidTokenException("토큰 처리 중 오류가 발생했습니다.");
    }
}