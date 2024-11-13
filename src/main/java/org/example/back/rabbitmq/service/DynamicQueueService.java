package org.example.back.rabbitmq.service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
@RequiredArgsConstructor
@Slf4j
public class DynamicQueueService  {
	private final RabbitAdmin rabbitAdmin;
	private final RabbitTemplate rabbitTemplate;
	private final ConsumerService consumerService;
	private final Jackson2JsonMessageConverter jsonMessageConverter;
	private final ConcurrentHashMap<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();

	// 채팅 관련 이름 생성을 위한 유틸리티 메소드들
	private String getExchangeName(Long chatRoomId) {
		return "chat_exchange_" + chatRoomId;
	}

	private String getQueueName(Long chatRoomId) {
		return "chat_queue_" + chatRoomId;
	}

	private String getRoutingKey(Long chatRoomId) {
		return "chat_route_" + chatRoomId;
	}

	public void createQueueAndListener(Long chatRoomId) {
		String exchangeName = getExchangeName(chatRoomId);
		String queueName = getQueueName(chatRoomId);
		String routingKey = getRoutingKey(chatRoomId);

		log.info("Creating queue - exchange: {}, queue: {}, routing: {}",
			exchangeName, queueName, routingKey);

		try {
			// Exchange, Queue, Binding 생성
			DirectExchange exchange = new DirectExchange(exchangeName, true, false);
			Queue queue = new Queue(queueName, true, false, false);
			Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);

			rabbitAdmin.declareExchange(exchange);
			rabbitAdmin.declareQueue(queue);
			rabbitAdmin.declareBinding(binding);

			// MessageListener 설정
			MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(consumerService, "handleMessage");
			listenerAdapter.setMessageConverter(jsonMessageConverter);

			// 리스너 컨테이너 생성 및 시작
			SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
			container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
			container.setQueueNames(queueName);
			container.setMessageListener(listenerAdapter);
			container.setAutoStartup(true);
			container.start();

			// 컨테이너 저장
			containers.put(queueName, container);

			log.info("Successfully created and started listener for queue: {}", queueName);

			// 큐 상태 확인
			Object queueProperties = rabbitAdmin.getQueueProperties(queueName);
			log.info("Queue {} status: {}", queueName, queueProperties != null ? "exists" : "not found");

		} catch (Exception e) {
			log.error("Error creating queue and listener: ", e);
			throw new RuntimeException("Failed to create queue and listener", e);
		}
	}
	public void removeQueueAndListener(Long chatRoomId) {
		String queueName = getQueueName(chatRoomId);
		SimpleMessageListenerContainer container = containers.remove(queueName);

		if (container != null) {
			container.stop();
			rabbitAdmin.deleteQueue(queueName);
			log.info("Removed listener and queue: {}", queueName);
		}
	}
}