package org.example.back.chat.common.dto;

public record ChatRoomAccessResponse(
	Long chatRoomId,
	Long partnerProfileId,
	boolean isAccessible
) {}