package org.example.back.chat.common.dto;

import java.time.LocalDateTime;

import org.example.back.chat.chatMessage.ChatMessage;
import org.example.back.chat.common.constant.MessageType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageRes extends MessageRes {
	private Long profileId;
	private String content;
	@JsonSerialize(using = LocalDateTimeSerializer.class)  // Jackson 직렬화 방식 지정
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime createdAt;
	private int unreadCnt;
	private FileInfo fileInfo;
	private Long chatRoomId;

	public ChatMessageRes(MessageType messageType, String id, Long profileId,
		String content, LocalDateTime createdAt,
		int unreadCnt, FileInfo fileInfo, Long chatRoomId) {
		super(messageType, id);
		this.profileId = profileId;
		this.content = content;
		this.createdAt = createdAt;
		this.unreadCnt = unreadCnt;
		this.fileInfo = fileInfo;
		this.chatRoomId=chatRoomId;
	}

	public static MessageRes createRes(ChatMessage message, int unreadCnt) {
		return new ChatMessageRes(
			message.getMessageType(),
			message.getId(),
			message.getProfileId(),
			message.getContent(),
			message.getCreatedAt(),
			unreadCnt,
			message.getFileInfo(),
			message.getChatRoomId()
		);
	}
}