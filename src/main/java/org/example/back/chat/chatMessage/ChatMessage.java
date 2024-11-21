package org.example.back.chat.chatMessage;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
@ToString
public class ChatMessage {

	@Id
	private String id;

	private Long chatRoomId;

	private Long profileId;

	private String content;

	@CreatedDate
	@Column(name = "createdAt", updatable = false)
	private LocalDateTime createdAt;
}