package org.example.back.chat.chatRoom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	@Query("SELECT c FROM ChatRoom c LEFT JOIN FETCH c.participants WHERE c.id = :chatRoomId")
	Optional<ChatRoom> findByIdWithParticipants(@Param("chatRoomId") Long chatRoomId);

}