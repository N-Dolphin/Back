package org.example.back.chat.chatMessage;

import org.example.back.chat.chatRoom.ChatRoom;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageRes;
import org.example.back.chat.common.dto.ChatRoomEnterRequest;
import org.example.back.chat.common.dto.MessageRes;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.List;

public interface ChatMessageService {
	// 초기 연결 처리 - 토큰 검증 및 기본 설정만 수행
	void handleInitialConnect(StompHeaderAccessor accessor);

	// 실제 채팅방 입장 처리
	void handleChatRoomEnter(StompHeaderAccessor accessor, ChatRoomEnterRequest request);

	ChatMessageRes sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq req);

	// void sendMessage(ChatMessage chatMessage, int unreadCnt, ChatRoom chatRoom);

	List<MessageRes> getChatMessages(Long chatRoomId);

	// void handleConnectMessage(StompHeaderAccessor accessor);

	void handleDisconnectMessage(StompHeaderAccessor accessor);

	void exitChatRoom(ChatRoom chatRoom, Long profileId);
}