package org.example.back.chat.chatRoom;

import java.util.List;

import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.SimpleChatRoomRecord;

public interface ChatRoomService {
	ChatDto.ChatRoomCreateRes createMatchedChatRoom(Long fromProfileId, Long toProfileId);
	List<SimpleChatRoomRecord> getSimpleChatRooms(Long profileId);
	void leaveChatRoom(Long chatRoomId, Long profileId);

	boolean isAccessibleChatRoom(Long chatRoomId, Long profileId);
}