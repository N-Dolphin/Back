package org.example.back.chat.chatroommember;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.example.back.chat.chatRoom.ChatRoom;

@Entity
@Table(name = "ChatRoomParticipant")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoomParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatRoomId", nullable = false)
	private ChatRoom chatRoom;

	@Column(nullable = false)
	private Long profileId;  // 현재 참가자의 profileId

	@Column(nullable = false)
	private Long partnerProfileId;  // 대화 상대방의 profileId

	private LocalDateTime lastEntryTime;

	public ChatRoomParticipant(ChatRoom chatRoom, Long profileId, Long partnerProfileId) {
		this.chatRoom = chatRoom;
		this.profileId = profileId;
		this.partnerProfileId = partnerProfileId;
		this.lastEntryTime = LocalDateTime.now();
	}

	public void updateLastEntryTime() {
		this.lastEntryTime = LocalDateTime.now();
	}

	public void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}
}