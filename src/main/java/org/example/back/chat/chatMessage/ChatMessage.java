package org.example.back.chat.chatMessage;

import java.time.LocalDateTime;

import org.apache.tomcat.jni.FileInfo;
import org.example.back.chat.common.constant.MessageType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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

	private MessageType messageType;
	private FileInfo fileInfo;  // 추가
}