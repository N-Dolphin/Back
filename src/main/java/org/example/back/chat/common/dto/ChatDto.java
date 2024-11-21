package org.example.back.chat.common.dto;

import java.time.LocalDateTime;

import org.example.back.chat.chatMessage.ChatMessage;
import org.example.back.chat.chatRoom.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatDto {
	/**
	 * 웹소켓 접속시 요청 Dto
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatMessageReq {
		private String content;

		public ChatMessage createChatMessage(Long chatRoomId, Long profileId) {
			ChatMessage chatMessage = ChatMessage.builder()
				.chatRoomId(chatRoomId)
				.profileId(profileId)
				.content(content)
				.createdAt(LocalDateTime.now())
				.build();
			return chatMessage;
		}
	}

	/**
	 * 채팅방 개설 요청 dto
	 */
	@Getter
	public static class ChatRoomCreateReq {
		private Long roomMakerId;
		private Long guestId;

		public ChatRoom createChatRoom() {
			return ChatRoom.emptyChatRoom();
		}
	}

	/**
	 * 채팅방 개설 성공시 응답 dto
	 */
	@Getter
	@Builder
	public static class ChatRoomCreateRes {
		private Long chatRoomId;
		private Long roomMakerId;
		private Long guestId;

		public static ChatRoomCreateRes createRes(Long chatRoomId, Long roomMakerId, Long guestId) {
			return ChatRoomCreateRes.builder()
				.chatRoomId(chatRoomId)
				.roomMakerId(roomMakerId)
				.guestId(guestId)
				.build();
		}
	}
}