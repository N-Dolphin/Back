package org.example.back.chat.common.interceptor;



import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

import org.example.back.chat.common.constant.TokenType;
import org.example.back.chat.util.StompHeaderAccessorUtil;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.exception.InvalidTokenException;
import org.example.back.user.service.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final StompHeaderAccessorUtil stompHeaderAccessorUtil;
	private final UserService userService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (accessor.getCommand() == CONNECT) {
			String token = stompHeaderAccessorUtil.extractToken(accessor, TokenType.ACCESS_TOKEN);

			// 토큰 검증 및 subject(userId) 추출
			try {
				String userId = jwtTokenProvider.extractSubject(token);
				Long profileId = userService.getProfileIdByUserId(Long.parseLong(userId));
				System.out.println("프로필 ID는!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + profileId);
				stompHeaderAccessorUtil.setMemberIdInSession(accessor, profileId);
			} catch (ExpiredJwtException e) {
				throw new InvalidTokenException("토큰이 만료되었습니다. 다시 로그인하세요.");
			} catch (Exception e) {
				throw new InvalidTokenException("토큰이 유효하지 않습니다. 다시 로그인하세요.");
			}

			// chatRoomId 처리는 초기 연결시에는 하지 않음
		}

		return message;
	}

	private Long extractChatRoomIdFromDestination(String destination) {
		// "/chat/rooms/{chatRoomId}" 형식에서 chatRoomId 추출
		if (destination == null) {
			throw new IllegalArgumentException("Destination is required");
		}
		String[] parts = destination.split("/");
		return Long.parseLong(parts[parts.length - 1]);
	}
}