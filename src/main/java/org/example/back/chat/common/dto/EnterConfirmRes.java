package org.example.back.chat.common.dto;

import lombok.Getter;

// 새로운 응답 클래스
@Getter
public class EnterConfirmRes {
	private final String messageType = "ENTER_CONFIRM";
	private final Long profileId;        // 입장한 사용자 ID
	private final Long chatRoomId;

	public EnterConfirmRes(Long profileId,Long chatRoomId) {
		this.profileId = profileId;
		this.chatRoomId = chatRoomId;
	}
}