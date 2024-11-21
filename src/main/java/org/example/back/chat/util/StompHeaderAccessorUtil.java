package org.example.back.chat.util;

import java.util.Optional;

import org.example.back.chat.common.constant.TokenType;
import org.example.back.user.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompHeaderAccessorUtil {
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;

	static final String CHAT_ROOM_ID = "chat-room-id";
	static final String MEMBER_ID = "member-id";
	private static final String BEARER = "Bearer ";

	public void setMemberIdInSession(StompHeaderAccessor accessor, Long memberId) {
		accessor.getSessionAttributes().put(MEMBER_ID, memberId);
	}

	public Long getMemberIdInSession(StompHeaderAccessor accessor) {
		return Optional.ofNullable((Long) accessor.getSessionAttributes().get(MEMBER_ID))
			.orElseThrow(() -> new RuntimeException("Stomp header session에 memberId가 존재하지 않습니다"));
	}

	public Long removeMemberIdInSession(StompHeaderAccessor accessor) {
		return Optional.ofNullable((Long) accessor.getSessionAttributes().remove(MEMBER_ID))
			.orElseThrow(() -> new RuntimeException("Stomp header session에 memberId가 존재하지 않습니다"));
	}

	public Long getChatRoomIdInHeader(StompHeaderAccessor accessor) {
		return Optional.ofNullable(accessor.getFirstNativeHeader(CHAT_ROOM_ID))
			.map(Long::valueOf)
			.orElseThrow(() -> new RuntimeException("Stomp header에 chat-room-id 존재하지 않습니다"));
	}

	public void setChatRoomIdInSession(StompHeaderAccessor accessor, Long chatRoomId) {
		accessor.getSessionAttributes().put(CHAT_ROOM_ID, chatRoomId);
	}

	public Long getChatRoomIdInSession(StompHeaderAccessor accessor) {
		return Optional.ofNullable((Long) accessor.getSessionAttributes().get(CHAT_ROOM_ID))
			.orElseThrow(() -> new RuntimeException("Stomp header session에 chat-room-id가 존재하지 않습니다"));
	}

	public Long removeChatRoomIdInSession(StompHeaderAccessor accessor) {
		Object chatRoomId = accessor.getSessionAttributes().remove(CHAT_ROOM_ID);
		return chatRoomId != null ? (Long) chatRoomId : null;

		//클라이언트가 채팅방에 입장하기 전에 연결이 끊어지는 경우를 안전하게 처리
	}

	public String extractToken(StompHeaderAccessor accessor, TokenType tokenType) {
		Optional<String> requestToken = switch (tokenType) {
			case ACCESS_TOKEN -> Optional.ofNullable(accessor.getFirstNativeHeader("Authorization"))
				.filter(token -> token.startsWith("Bearer "))
				.map(token -> token.substring(7));
			case REFRESH_TOKEN -> Optional.ofNullable(accessor.getFirstNativeHeader("Refresh"))
				.filter(token -> token.startsWith("Bearer "))
				.map(token -> token.substring(7));
			default -> throw new IllegalStateException("Unexpected value: " + tokenType);
		};

		return requestToken.orElseThrow(() -> new InvalidTokenException("토큰이 존재하지 않습니다."));
	}
}