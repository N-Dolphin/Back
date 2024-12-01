package org.example.back.chat.common.constant;

// public enum MessageType {
// 	CHAT_MESSAGE,          // 일반 텍스트 메시지
// 	CHAT_SYNC_REQUEST,     // 동기화 요청
// 	IMAGE_MESSAGE,         // 이미지 메시지
// 	FILE_MESSAGE,         // 파일 메시지
// 	VIDEO_MESSAGE         // 비디오 메시지
// }

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType {
	@JsonProperty("CHAT")
	CHAT_MESSAGE,
	CHAT_SYNC_REQUEST,
	@JsonProperty("IMAGE")
	IMAGE_MESSAGE,
	@JsonProperty("FILE")
	FILE_MESSAGE,
	@JsonProperty("VIDEO")
	VIDEO_MESSAGE
}