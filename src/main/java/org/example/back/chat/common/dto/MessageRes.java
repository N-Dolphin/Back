package org.example.back.chat.common.dto;

import org.example.back.chat.common.constant.MessageType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MessageRes {
	MessageType messageType;
	//String id 추가

	protected MessageRes() {  // 기본 생성자 추가
		this.messageType = MessageType.CHAT_MESSAGE;
	}

	protected MessageRes(MessageType messageType) {
		this.messageType = messageType;
	}
}