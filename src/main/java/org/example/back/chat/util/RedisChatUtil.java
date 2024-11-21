package org.example.back.chat.util;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class RedisChatUtil {

	private final RedisTemplate<Long, Long> chatRoom2Members;

	public void addChatRoom2Member(Long chatRoomId, Long memberId) {
		SetOperations<Long, Long> ops = chatRoom2Members.opsForSet();
		ops.add(chatRoomId, memberId);
	}

	public Set<Long> getOnlineMembers(Long chatRoomId) {
		SetOperations<Long, Long> ops = chatRoom2Members.opsForSet();
		return ops.members(chatRoomId);
	}

	public int getOnlineMemberCntInChatRoom(Long chatRoomId) {
		SetOperations<Long, Long> ops = chatRoom2Members.opsForSet();
		return ops.members(chatRoomId).size();
	}

	public void removeChatRoom2Member(Long chatRoomId, Long memberId) {
		SetOperations<Long, Long> ops = chatRoom2Members.opsForSet();
		ops.remove(chatRoomId, memberId);
	}
}