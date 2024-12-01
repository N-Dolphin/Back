package org.example.back.chat.config;


import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.example.back.chat.common.interceptor.JwtAuthenticationInterceptor;
import org.example.back.config.interceptor.JwtInterceptor;
import org.example.back.config.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

	@Value("${spring.rabbitmq.relay.host}")
	private String relayHost;

	@Value("${spring.rabbitmq.relay.port}")
	private Integer relayPort;

	@Value("${spring.rabbitmq.relay.client-login}")
	private String clientLogin;

	@Value("${spring.rabbitmq.relay.client-passcode}")
	private String clientPasscode;

	@Value("${spring.rabbitmq.relay.system-login}")
	private String systemLogin;

	@Value("${spring.rabbitmq.relay.system-passcode}")
	private String systemPasscode;



	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		registry.setMessageSizeLimit(2 * 1024 * 1024)    // 2MB
			.setSendBufferSizeLimit(4 * 1024 * 1024)  // 4MB
			.setSendTimeLimit(20 * 1000);             // 20 seconds

		// 기존 디버그 로그도 유지
		registry.addDecoratorFactory(webSocketHandler -> {
			return new WebSocketHandlerDecorator(webSocketHandler) {
				@Override
				public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
					System.out.println("Message payload size: " + message.getPayloadLength());
					super.handleMessage(session, message);
				}
			};
		});
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/chat/inbox")
			.setAllowedOriginPatterns("*");
	}


	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지 브로커 설정
		registry.setPathMatcher(new AntPathMatcher(".")); // url을 chat/room/3 -> chat.room.3으로 참조하기 위한 설정

		registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
			.setRelayHost(relayHost)
			.setRelayPort(relayPort)
			.setSystemLogin(systemLogin)
			.setSystemPasscode(systemPasscode)
			.setClientLogin(clientLogin)
			.setClientPasscode(clientPasscode)
			.setSystemHeartbeatSendInterval(30000)
			.setSystemHeartbeatReceiveInterval(30000);

		// 클라이언트로부터 메시지를 받을 api의 prefix를 설정함
		// publish
		registry.setApplicationDestinationPrefixes("/pub");

	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(jwtAuthenticationInterceptor);
	}


	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(2 * 1024 * 1024);
		container.setMaxBinaryMessageBufferSize(2 * 1024 * 1024);
		return container;
	}
}