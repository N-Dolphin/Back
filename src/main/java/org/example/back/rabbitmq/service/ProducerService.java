package org.example.back.rabbitmq.service;

import org.example.back.rabbitmq.MessageDto;

public interface ProducerService {

	// 메시지를 큐로 전송 합니다.
	void sendMessage(MessageDto messageDto);
}
