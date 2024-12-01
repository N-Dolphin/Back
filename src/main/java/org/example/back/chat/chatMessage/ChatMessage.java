package org.example.back.chat.chatMessage;

import java.time.LocalDateTime;
import org.example.back.chat.common.constant.MessageType;
import org.example.back.chat.common.dto.FileInfo;
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

// @Getter
// @Setter
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
// @Document(collection = "chat_message")
// @ToString
// public class ChatMessage {
//
// 	@Id
// 	private String id;
//
// 	private Long chatRoomId;
//
// 	private Long profileId;
//
// 	private String content;
//
// 	@CreatedDate
// 	@Column(name = "createdAt", updatable = false)
// 	private LocalDateTime createdAt;
//
// 	private MessageType messageType;
// 	private FileInfo fileInfo;  // 추가
// }

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
	private String content;      // 텍스트 메시지 또는 S3 URL

	@CreatedDate
	@Column(name = "createdAt", updatable = false)
	private LocalDateTime createdAt;

	@Builder.Default
	private MessageType messageType = MessageType.CHAT_MESSAGE;

	@Builder.Default
	private FileInfo fileInfo = null;  // 기존 FileInfo 클래스 사용
}