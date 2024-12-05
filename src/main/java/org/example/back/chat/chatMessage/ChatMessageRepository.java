package org.example.back.chat.chatMessage;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
	Optional<ChatMessage> findByChatRoomIdAndProfileIdAndId(
		Long chatRoomId,
		Long profileId,
		String id
	);

	// 페이징 처리를 위한 메서드 추가
	Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(
		Long chatRoomId,
		Pageable pageable
	);

	// 특정 시간 이후의 메시지 중 특정 사용자가 보내지 않은 메시지 개수
	Long countByChatRoomIdAndProfileIdNotAndCreatedAtAfter(
		Long chatRoomId,
		Long profileId,
		LocalDateTime lastEntryTime
	);

	void deleteByChatRoomId(Long chatRoomId);

}