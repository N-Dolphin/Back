package org.example.back.rabbitmq.service;

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
@RequiredArgsConstructor
@Slf4j
public class DynamicQueueService {
	private final RabbitAdmin rabbitAdmin;
	private final RabbitTemplate rabbitTemplate;
	private final ConsumerService consumerService;
	private final Jackson2JsonMessageConverter jsonMessageConverter;


	private final ConcurrentHashMap<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();

	public void createQueueAndListener(Long fromProfileId, Long toProfileId, Long chatRoomId) {
		String exchangeName = "exchange_" + fromProfileId + "_" + toProfileId;
		String queueName = "chat_room_" + chatRoomId;
		String routingKey = "route_" + fromProfileId + "_" + toProfileId;

		// Exchange, Queue, Binding 생성
		DirectExchange exchange = new DirectExchange(exchangeName);
		Queue queue = new Queue(queueName, true);
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);

		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareBinding(binding);

		// MessageListener 설정 - JSON 컨버터 적용
		MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(consumerService, "receiveMessage");
		listenerAdapter.setMessageConverter(jsonMessageConverter);

		// Container 생성 및 시작
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		container.start();

		// 컨테이너 저장
		containers.put(queueName, container);

		log.info("Created and started listener for queue: {}", queueName);
	}

	public void removeQueueAndListener(Long chatRoomId) {
		String queueName = "chat_room_" + chatRoomId;
		SimpleMessageListenerContainer container = containers.remove(queueName);

		if (container != null) {
			container.stop();
			rabbitAdmin.deleteQueue(queueName);
			log.info("Removed listener and queue: {}", queueName);
		}
	}
}