package org.example.back.chat.common.dto;

import org.example.back.chat.common.constant.MessageType;

import lombok.Getter;

@Getter
public abstract class MessageRes {
	MessageType messageType;

	protected MessageRes(MessageType messageType) {
		this.messageType = messageType;
	}
}