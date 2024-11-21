package org.example.back.chat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor  // 기본 생성자 필수
@AllArgsConstructor
public class ChatRoomEnterRequest {
	private Long chatRoomId;
	private String messageType;
}