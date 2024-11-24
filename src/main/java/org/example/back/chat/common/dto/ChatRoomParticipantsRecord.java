package org.example.back.chat.common.dto;

import java.util.List;

public record ChatRoomParticipantsRecord(
	Long chatRoomId,
	List<Long> participantIds
) {}