package org.example.back.chat.entity;

import java.time.LocalDateTime;

import org.example.back.rabbitmq.MessageDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long senderId;

	@Column
	private Long receiverId;

	@Column
	private String content;

	@Column
	private LocalDateTime sentAt = LocalDateTime.now();

	@Column
	private LocalDateTime deliveredAt;

	@Column
	private LocalDateTime readAt;

	@Enumerated(EnumType.STRING)
	private MessageStatus status = MessageStatus.SENT;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatroom_id", nullable = false)
	private ChatRoom chatRoom;

	public void markAsDelivered() {
		this.deliveredAt = LocalDateTime.now();
		this.status = MessageStatus.DELIVERED;
	}

	public void markAsRead() {
		this.readAt = LocalDateTime.now();
		this.status = MessageStatus.READ;
	}

	public static ChatMessage of(Long senderId, Long receiverId, String content, ChatRoom chatRoom) {
		var chatMessage = new ChatMessage();
		chatMessage.setSenderId(senderId);
		chatMessage.setReceiverId(receiverId);
		chatMessage.setContent(content);
		chatMessage.setChatRoom(chatRoom);
		chatMessage.setSentAt(LocalDateTime.now());
		chatMessage.setStatus(MessageStatus.SENT);
		return chatMessage;
	}

	public enum MessageStatus {
		SENT,       // 메시지 전송됨
		DELIVERED,  // 상대방의 디바이스에 전달됨
		READ,       // 상대방이 읽음
		FAILED      // 전송 실패
	}
}