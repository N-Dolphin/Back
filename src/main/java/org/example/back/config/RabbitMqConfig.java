package org.example.back.config;
import org.example.back.rabbitmq.service.ConsumerService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMqConfig {

	private static final String CHAT_EXCHANGE = "chat.direct.exchange";


	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
		ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setConcurrentConsumers(1);
		factory.setMaxConcurrentConsumers(10);
		factory.setAutoStartup(true);
		factory.setDefaultRequeueRejected(false);
		factory.setAdviceChain(RetryInterceptorBuilder
			.stateless()
			.maxAttempts(3)
			.backOffOptions(1000, 2.0, 10000)
			.build());
		return factory;
	}

	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	// @Bean
	// public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
	// 	RabbitTemplate template = new RabbitTemplate(connectionFactory);
	// 	template.setMessageConverter(jsonMessageConverter());
	// 	template.setConfirmCallback((correlationData, ack, cause) -> {
	// 		if (!ack) {
	// 			log.error("Message send failed: {}", cause);
	// 		}
	// 	});
	// 	return template;
	// }
	@Bean
	public DirectExchange chatExchange() {
		// 단일 Exchange 생성
		return new DirectExchange(CHAT_EXCHANGE, true, false);
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setExchange(CHAT_EXCHANGE);
		template.setMessageConverter(new Jackson2JsonMessageConverter());
		return template;
	}

	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}
}