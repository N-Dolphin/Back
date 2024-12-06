package org.example.back.chat.common.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDeleteRequest {
	private Long chatRoomId;
	private String messageId;
}