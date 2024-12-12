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

	public Set<Long> getOnlineMembers(Long chatRoomId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		Set<String> members = ops.members(getChatRoomKey(chatRoomId));
		return members.stream()
			.map(Long::parseLong)
			.collect(Collectors.toSet());
	}

	public void addOnlineMember(Long chatRoomId, Long memberId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		ops.add(getChatRoomKey(chatRoomId), String.valueOf(memberId));
	}

	public void removeChatRoom2Member(Long chatRoomId, Long memberId) {
		SetOperations<String, String> ops = redisTemplate.opsForSet();
		ops.remove(getChatRoomKey(chatRoomId), String.valueOf(memberId));
	}

	// 채팅방 완전 삭제
	public void deleteChatRoom(Long chatRoomId) {
		String chatRoomKey = getChatRoomKey(chatRoomId);
		// 채팅방 관련 모든 Redis 데이터 삭제
		redisTemplate.delete(chatRoomKey);
	}
}