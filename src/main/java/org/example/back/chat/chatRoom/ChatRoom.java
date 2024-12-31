package org.example.back.chat.chatRoom;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ChatRoom")
@Getter
@Setter
@DynamicUpdate
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ChatRoomParticipant> participants = new HashSet<>();

	public static ChatRoom emptyChatRoom() {
		return new ChatRoom();
	}

	public void addParticipant(ChatRoomParticipant participant) {
		if (Optional.ofNullable(participants).isEmpty()) {
			participants = new HashSet<>();
		}
		if (participant.getChatRoom() != this) {
			participant.setChatRoom(this);
		}
		this.participants.add(participant);
	}

	public int getParticipantCount() {
		return Optional.ofNullable(participants).map(Set::size).orElse(0);
	}

	public ChatRoomParticipant getParticipant(Long profileId) {
		return participants.stream()
			.filter(participant -> participant.getProfileId().equals(profileId))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("채팅방 참가자를 찾을 수 없습니다"));
	}

	public int getUnreadCount(Set<Long> onlineProfiles, LocalDateTime messageCreatedAt) {
		// Set<Long> 타입을 그대로 사용하도록 수정
		List<LocalDateTime> lastEntryTimes = participants.stream()
			.filter(participant -> !onlineProfiles.contains(participant.getProfileId()))
			.map(ChatRoomParticipant::getLastEntryTime)
			.toList();

		int unreadCount = (int) lastEntryTimes.stream()
			.filter(time -> time.isAfter(messageCreatedAt))
			.count();

		return participants.size() - onlineProfiles.size() - unreadCount;
	}
}