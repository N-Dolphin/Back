package org.example.back.chat.common.dto;

import java.time.LocalDateTime;

import org.example.back.chat.chatMessage.ChatMessage;
import org.example.back.chat.common.constant.MessageType;

import lombok.AllArgsConstructor;
import lombok.Getter;

// @Getter
// public class ChatMessageRes extends MessageRes {
// 	static private final MessageType messageType = MessageType.CHAT_MESSAGE;
// 	private Long profileId;
// 	private String content;
// 	private LocalDateTime createdAt;
// 	private int unreadCnt;
//
// 	private ChatMessageRes(Long profileId, String content, LocalDateTime createdAt, int unreadCnt) {
// 		super(messageType);
// 		this.profileId = profileId;
// 		this.content = content;
// 		this.createdAt = createdAt;
// 		this.unreadCnt = unreadCnt;
// 	}
//
// 	public static MessageRes createRes(ChatMessage chatMessage, int unreadCnt) {
// 		return new ChatMessageRes(chatMessage.getProfileId(), chatMessage.getContent(), chatMessage.getCreatedAt(), unreadCnt);
// 	}
// }
@Getter
public class ChatMessageRes extends MessageRes {
	private Long profileId;
	private String content;
	private LocalDateTime createdAt;
	private int unreadCnt;
	private FileInfo fileInfo;

	public ChatMessageRes(MessageType messageType, Long profileId, String content,
		LocalDateTime createdAt, int unreadCnt, FileInfo fileInfo) {
		super(messageType);  // 부모 클래스 생성자 호출
		this.profileId = profileId;
		this.content = content;
		this.createdAt = createdAt;
		this.unreadCnt = unreadCnt;
		this.fileInfo = fileInfo;
	}

	public static MessageRes createRes(ChatMessage message, int unreadCnt) {
		return new ChatMessageRes(
			message.getMessageType(),
			message.getProfileId(),
			message.getContent(),
			message.getCreatedAt(),
			unreadCnt,
			message.getFileInfo()
		);
	}
}