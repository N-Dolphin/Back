package org.example.back.chat.chatroommember;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
	List<ChatRoomParticipant> findAllByProfileId(Long profileId);
}