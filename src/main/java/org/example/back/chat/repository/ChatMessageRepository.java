package org.example.back.chat.repository;

import java.util.List;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	// 커서 기반 페이징 적용
	@Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.id < :cursor ORDER BY m.id DESC")
	List<ChatMessage> findByChatRoomIdAndIdLessThan(
		@Param("roomId") Long roomId,
		@Param("cursor") Long cursor,
		Pageable pageable
	);

	// 첫 페이지 조회용
	@Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId ORDER BY m.id DESC")
	List<ChatMessage> findByChatRoomId(@Param("roomId") Long roomId, Pageable pageable);

	// 메시지 카운트
	@Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :roomId")
	long countByChatRoomId(@Param("roomId") Long roomId);
}