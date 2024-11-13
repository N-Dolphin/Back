package org.example.back.chat.config;

import java.security.Principal;
import java.util.Map;

import org.example.back.config.interceptor.JwtInterceptor;
import org.example.back.config.interceptor.LogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final WebSocketAuthInterceptor webSocketAuthInterceptor;
	private final CorsProperties corsProperties;

	@Value("${server.serverAddress}")
	private String serverAddress;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/queue");
		config.setApplicationDestinationPrefixes("/app");
		config.setUserDestinationPrefix("/user");
	}

	// @Override
	// public void registerStompEndpoints(StompEndpointRegistry registry) {
	// 	registry.addEndpoint("/api/v1/ws-chat")
	// 		.addInterceptors(webSocketAuthInterceptor)
	// 		.setAllowedOriginPatterns(
	// 			"http://localhost:8080",
	// 			"http://localhost:3000",
	// 			"http://127.0.0.1:8080",
	// 			"http://127.0.0.1:3000"
	// 		)
	// 		.withSockJS()
	// 		.setWebSocketEnabled(true)
	// 		.setDisconnectDelay(30 * 1000)
	// 		.setHeartbeatTime(25 * 1000)
	// 		.setSessionCookieNeeded(false);  // 추가
	// }
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/api/v1/ws-chat")
			.addInterceptors(webSocketAuthInterceptor)
			.setAllowedOriginPatterns(corsProperties.getAllowedOrigins().toArray(String[]::new))
			.withSockJS()
			.setWebSocketEnabled(true)
			.setDisconnectDelay(30 * 1000)
			.setHeartbeatTime(25 * 1000)
			.setSessionCookieNeeded(false);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					Object raw = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
					if (raw instanceof Map) {
						Object profileId = accessor.getSessionAttributes().get("profileId");
						if (profileId != null) {
							accessor.setUser(new Principal() {
								@Override
								public String getName() {
									return profileId.toString();
								}
							});
							log.debug("Set user principal with profile ID: {}", profileId);
						}
					}
				}
				return message;
			}
		});
	}
}