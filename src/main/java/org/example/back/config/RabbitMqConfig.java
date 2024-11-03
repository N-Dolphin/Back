package org.example.back.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

	@Value("${spring.rabbitmq.host}")
	private String rabbitmqHost;

	@Value("${spring.rabbitmq.port}")
	private int rabbitmqPort;

	@Value("${spring.rabbitmq.username}")
	private String rabbitmqUsername;

	@Value("${spring.rabbitmq.password}")
	private String rabbitmqPassword;



	/**
	 * RabbitMQ 연결을 위한 ConnectionFactory 빈을 생성
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(rabbitmqHost);
		connectionFactory.setPort(rabbitmqPort);
		connectionFactory.setUsername(rabbitmqUsername);
		connectionFactory.setPassword(rabbitmqPassword);
		return connectionFactory;
	}

	/**
	 * RabbitTemplate 빈을 생성하고 JSON 형식의 메시지를 직렬화/역직렬화 가능하게 설정
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jackson2JsonMessageConverter());
		return template;
	}

	/**
	 * 메시지를 JSON 형식으로 변환하는 MessageConverter 빈
	 */
	@Bean
	public MessageConverter jackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * RabbitAdmin 빈을 통해 동적 Queue, Exchange 및 Binding을 선언할 수 있도록 설정
	 */
	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	/**
	 * 동적 Queue 및 Exchange 생성을 위한 메서드
	 */
	public void declareChatRoomQueueAndExchange(Long fromProfileId, Long toProfileId) {
		String dynamicExchangeName = "exchange_" + fromProfileId + "_" + toProfileId;
		String dynamicQueueName = "queue_" + fromProfileId + "_" + toProfileId;

		// Exchange 및 Queue를 선언
		DirectExchange exchange = new DirectExchange(dynamicExchangeName);
		Queue queue = new Queue(dynamicQueueName);

		// RabbitAdmin을 사용하여 exchange와 queue를 선언
		rabbitAdmin(connectionFactory()).declareExchange(exchange);
		rabbitAdmin(connectionFactory()).declareQueue(queue);

		// Exchange와 Queue를 바인딩
		Binding binding = BindingBuilder.bind(queue).to(exchange).with("");
		rabbitAdmin(connectionFactory()).declareBinding(binding);
	}
}
