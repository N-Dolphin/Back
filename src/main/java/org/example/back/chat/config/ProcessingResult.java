package org.example.back.chat.config;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProcessingResult {
	private final ChatMessage chatMessage;
	private final ChatRoom chatRoom;
}