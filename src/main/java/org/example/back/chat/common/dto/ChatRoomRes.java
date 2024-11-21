package org.example.back.chat.common.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import org.example.back.chat.chatMessage.ChatMessage;

import lombok.Builder;

@Builder
public record ChatRoomRes(
	Long chatRoomId,
	Long profileId,
	int unreadMessageCnt,
	String lastMessage,
	LocalDateTime createdAt
) {
	public static ChatRoomRes createRes(Long chatRoomId, Long profileId, int unreadMessageCnt, Optional<ChatMessage> latestMessage) {
		return ChatRoomRes.builder()
			.chatRoomId(chatRoomId)
			.profileId(profileId)
			.unreadMessageCnt(unreadMessageCnt)
			.lastMessage(latestMessage.map(ChatMessage::getContent).orElse(null))
			.createdAt(latestMessage.map(ChatMessage::getCreatedAt).orElse(null))
			.build();
	}
}