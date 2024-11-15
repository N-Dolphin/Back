package org.example.back.chat.config;

import java.security.Principal;
import java.util.Map;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.service.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

		log.debug("Starting WebSocket handshake...");

		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
			HttpServletRequest httpRequest = servletRequest.getServletRequest();

			String token = httpRequest.getParameter("token");

			if (token != null) {
				try {
					String userIdStr = jwtTokenProvider.extractSubject(token);
					Long userId = Long.valueOf(userIdStr);
					Long profileId = userService.getProfileIdByUserId(userId);

					log.debug("Found profile ID: {}", profileId);
					attributes.put("profileId", profileId);

					// Principal 설정 추가
					attributes.put("user", new Principal() {
						@Override
						public String getName() {
							return profileId.toString();
						}
					});

					return true;
				} catch (Exception e) {
					log.error("Failed to process token: ", e);
					return false;
				}
			}
		}
		return false;
	}
	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
		if (exception != null) {
			log.error("After handshake error: ", exception);
		}
	}

	private String resolveToken(HttpServletRequest request) {
		String token = request.getHeader("sec-websocket-protocol");
		log.debug("sec-websocket-protocol header: {}", token);

		if (token == null) {
			// URL 파라미터에서 토큰 확인
			token = request.getParameter("token");
			log.debug("URL parameter token: {}", token);
		}

		if (token != null && token.startsWith("Bearer ")) {
			return token.substring(7);
		}
		return token;
	}
}
