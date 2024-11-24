package org.example.back.chat.common.dto;

public record SimpleChatRoomRecord(
	Long chatRoomId,
	Long myProfileId,
	Long partnerProfileId
) {}