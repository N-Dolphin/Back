package org.example.back.chat.common.constant;


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
	VIDEO_MESSAGE,
	@JsonProperty("DELETE")
	DELETED_MESSAGE
}