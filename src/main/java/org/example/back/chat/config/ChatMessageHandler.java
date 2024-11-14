package org.example.back.chat.config;

import org.springframework.stereotype.Component;

@Component
public interface ChatMessageHandler {
	void handleMessage(String message);
}