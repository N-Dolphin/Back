package org.example.back.rabbitmq.service;

import org.example.back.rabbitmq.MessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService{

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	@Override
	public void sendMessage(MessageDto messageDto) {
		try {
			// 객체를 JSON으로 변환
			ObjectMapper objectMapper = new ObjectMapper();
			String objectToJSON = objectMapper.writeValueAsString(messageDto);
			System.out.println("전송할 메시지: " + objectToJSON);
			rabbitTemplate.convertAndSend(exchangeName, routingKey, objectToJSON);
			System.out.println("성공적으로 메시지를 전송했습니다.");



		} catch (JsonProcessingException jpe) {
			System.out.println("파싱 오류 발생: " + jpe.getMessage());
		}
	}
}

