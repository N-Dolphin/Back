package org.example.back.chat.util;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class RedisChatUtil {
	private final RedisTemplate<String, String> redisTemplate;

	// 채팅방 키 prefix 추가
	private String getChatRoomKey(Long chatRoomId) {
		return "chat:room:" + chatRoomId;
	}

	public void addChatRoom2Member(Long chatRoomId, Long memberId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		ops.add(getChatRoomKey(chatRoomId), String.valueOf(memberId));
	}

	public Set<Long> getOnlineMembers(Long chatRoomId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		Set<String> members = ops.members(getChatRoomKey(chatRoomId));
		return members.stream()
			.map(Long::parseLong)
			.collect(Collectors.toSet());
	}

	public int getOnlineMemberCntInChatRoom(Long chatRoomId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		Set<String> members = ops.members(getChatRoomKey(chatRoomId));
		return members != null ? members.size() : 0;
	}

	public void removeChatRoom2Member(Long chatRoomId, Long memberId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		ops.remove(getChatRoomKey(chatRoomId), String.valueOf(memberId));
	}
}