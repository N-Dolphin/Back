package org.example.back.chat.config;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitConfig {

	@Value("${spring.rabbitmq.chat.queue.name}")
	private String chatQueueName;
	@Value("${spring.rabbitmq.chat.exchange.name}")
	private String chatExchangeName;
	@Value("${spring.rabbitmq.chat.routing.key}")
	private String routingKey;

	@Value("${spring.rabbitmq.host}")
	private String host;
	@Value("${spring.rabbitmq.port}")
	private int port;
	@Value("${spring.rabbitmq.virtual-host}")
	private String virtualHost;
	@Value("${spring.rabbitmq.username}")
	private String username;
	@Value("${spring.rabbitmq.password}")
	private String password;



	// Queue 등록
	@Bean
	public Queue queue() {
		return new Queue(chatQueueName, true, false, false);
	}

	// Exchange 등록
	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(chatExchangeName);
	}

	// Exchange와 Queue바인딩
	@Bean
	public Binding binding(Queue queue, TopicExchange exchange){
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routingKey);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}


	//메세지 통신 담당 클래스
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	// RabbitMQ와의 연결을 관리하는 클래스
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		factory.setVirtualHost(virtualHost);
		factory.setUsername(username);
		factory.setPassword(password);
		return factory;
	}
}