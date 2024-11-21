package org.example.back.chat.chatRoom;

import java.util.List;

import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatRoomRes;

public interface ChatRoomService {
	ChatDto.ChatRoomCreateRes createChatRoomForPersonal(Long id, ChatDto.ChatRoomCreateReq request);

	List<ChatRoomRes> getChatRooms(Long loginId);

	ChatDto.ChatRoomCreateRes createMatchedChatRoom(Long fromProfileId, Long toProfileId);

	List<ChatRoomRes> getChatRoomsByProfileId(Long profileId);
}