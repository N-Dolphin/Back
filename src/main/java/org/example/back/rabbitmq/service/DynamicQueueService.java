package org.example.back.rabbitmq.service;

import org.example.back.chat.config.ChatMessageHandler;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicQueueService {
	private static final String CHAT_EXCHANGE = "chat.direct.exchange";
	private final RabbitAdmin rabbitAdmin;
	private final RabbitTemplate rabbitTemplate;
	private final ChatMessageHandler messageHandler;
	private final Jackson2JsonMessageConverter jsonMessageConverter;
	private final ConcurrentHashMap<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();

	public void createQueueAndListener(Long chatRoomId) {
		String queueName = getQueueName(chatRoomId);
		String routingKey = getRoutingKey(chatRoomId);

		try {
			// 큐 생성
			Queue queue = new Queue(queueName, true, false, false);

			// 단일 Exchange에 binding
			Binding binding = BindingBuilder.bind(queue)
				.to(new DirectExchange(CHAT_EXCHANGE))
				.with(routingKey);

			// 큐와 바인딩 선언
			rabbitAdmin.declareQueue(queue);
			rabbitAdmin.declareBinding(binding);

			// 리스너 설정
			MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(messageHandler, "handleMessage");
			listenerAdapter.setMessageConverter(jsonMessageConverter);

			SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
			container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
			container.setQueueNames(queueName);
			container.setMessageListener(listenerAdapter);
			container.start();

			containers.put(queueName, container);
			log.info("Created queue and binding for chatRoom: {}", chatRoomId);

		} catch (Exception e) {
			log.error("Failed to create queue and binding", e);
			throw new RuntimeException("Queue creation failed", e);
		}
	}

	private String getQueueName(Long chatRoomId) {
		return String.format("chat.queue.%d", chatRoomId);
	}

	private String getRoutingKey(Long chatRoomId) {
		return String.format("chat.room.%d", chatRoomId);
	}

	public void removeQueueAndListener(Long chatRoomId) {
		String queueName = getQueueName(chatRoomId);
		SimpleMessageListenerContainer container = containers.remove(queueName);

		if (container != null) {
			container.stop();
			rabbitAdmin.deleteQueue(queueName);
			log.info("Removed queue and listener for chatRoom: {}", chatRoomId);
		}
	}
}