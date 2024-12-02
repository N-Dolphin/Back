package org.example.back.chat.common.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDeleteRequest {
	private Long chatRoomId;
	private LocalDateTime timestamp;
}





// profileId - 이름 나이 프로필 사진
