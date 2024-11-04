package org.example.back.chat.repository;

import java.util.List;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByChatRoomId(Long chatRoomId);

	long countByChatRoomId(Long chatRoomId); // 추가

}
