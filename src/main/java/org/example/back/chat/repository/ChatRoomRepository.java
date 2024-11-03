package org.example.back.chat.repository;

import java.util.List;
import java.util.Optional;

import org.example.back.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	Optional<ChatRoom> findByFromProfileIdAndToProfileId(Long fromProfileId, Long toProfileId);

	List<ChatRoom> findAllByFromProfileIdOrToProfileId(Long fromProfileId, Long toProfileId);

	@Query("SELECT c.fromProfileId, c.toProfileId FROM ChatRoom c WHERE c.id = :id")
	List<Object[]> findProfileIdsByChatRoomId(@Param("id") Long id);

	@Query("SELECT c.id FROM ChatRoom c WHERE (c.fromProfileId = :fromProfileId AND c.toProfileId = :toProfileId) OR (c.fromProfileId = :toProfileId AND c.toProfileId = :fromProfileId)")
	Optional<Long> findChatRoomIdByProfileIds(@Param("fromProfileId") Long fromProfileId, @Param("toProfileId") Long toProfileId);


}