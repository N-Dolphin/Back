package org.example.back.chat.chatMessage;

import org.example.back.chat.chatRoom.ChatRoom;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageRes;
import org.example.back.chat.common.dto.ChatRoomEnterRequest;
import org.example.back.chat.common.dto.MessageRes;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageService {
	void handleInitialConnect(StompHeaderAccessor accessor);
	ChatMessageRes sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq req);
	void handleDisconnectMessage(StompHeaderAccessor accessor);
	void exitChatRoom(ChatRoom chatRoom, Long profileId);
	void deleteMessage(Long chatRoomId, Long profileId, String messageId);
	List<MessageRes> getChatMessages(Long chatRoomId, int page, int size);
}