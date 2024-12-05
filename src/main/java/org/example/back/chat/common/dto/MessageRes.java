package org.example.back.chat.common.dto;

import org.example.back.chat.common.constant.MessageType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MessageRes {
	private MessageType messageType;
	private String id;

	protected MessageRes() {
		this.messageType = MessageType.CHAT_MESSAGE;
	}

	// id가 없는 생성자 추가
	protected MessageRes(MessageType messageType) {
		this.messageType = messageType;
	}

	protected MessageRes(MessageType messageType, String id) {
		this.messageType = messageType;
		this.id = id;
	}
}