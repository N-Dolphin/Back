package org.example.back.chat.common.dto;

import java.time.LocalDateTime;

import org.example.back.chat.chatMessage.ChatMessage;
import org.example.back.chat.common.constant.MessageType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageRes extends MessageRes {
	private String id;
	private Long profileId;
	private String content;
	private LocalDateTime createdAt;
	private int unreadCnt;
	private FileInfo fileInfo;

	public ChatMessageRes(MessageType messageType, String id, Long profileId,
		String content, LocalDateTime createdAt,
		int unreadCnt, FileInfo fileInfo) {
		super(messageType);
		this.id = id;
		this.profileId = profileId;
		this.content = content;
		this.createdAt = createdAt;
		this.unreadCnt = unreadCnt;
		this.fileInfo = fileInfo;
	}

	public static MessageRes createRes(ChatMessage message, int unreadCnt) {
		return new ChatMessageRes(
			message.getMessageType(),
			message.getId(),
			message.getProfileId(),
			message.getContent(),
			message.getCreatedAt(),
			unreadCnt,
			message.getFileInfo()
		);
	}
}