package org.example.back.chat.entity;

import java.time.LocalDateTime;

import org.example.back.rabbitmq.MessageDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Getter@Setter
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long senderId;    // 메시지를 보낸 사용자 ID

	@Column
	private Long receiverId;    // 메시지를 보낸 사용자 ID

	@Column
	private String content;    // 메시지 내용

	@Column
	private LocalDateTime sentAt = LocalDateTime.now(); // 메시지 전송 시간

	// ChatRoom과의 다대일 관계 설정
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatroom_id", nullable = false)
	private ChatRoom chatRoom;

	public static ChatMessage of(Long senderId, Long receiverId, String content, ChatRoom chatRoom){
		var chatMessage= new ChatMessage();
		chatMessage.setSenderId(senderId);
		chatMessage.setReceiverId(receiverId);
		chatMessage.setContent(content);
		chatMessage.setChatRoom(chatRoom);
		return chatMessage;
	}
}
