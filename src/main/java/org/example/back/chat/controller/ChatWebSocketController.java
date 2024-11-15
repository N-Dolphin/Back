package org.example.back.chat.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.example.back.chat.dto.ChatMessageDto;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.service.ChatRoomService;
import org.example.back.chat.service.MessageService;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController implements ChatWebSocketControllerSwagger {
	private final ChatRoomService chatRoomService;
	private final SimpMessagingTemplate messagingTemplate;
	private final MessageService messageService;
	private final Set<String> connectedSessions = ConcurrentHashMap.newKeySet();

	@Override
	public void connectWebSocket() {
		// Swagger 문서화용 메서드
		log.debug("WebSocket connection endpoint documentation");
	}

	@Override
	@MessageMapping("/chat/{roomId}/sendMessage")
	public void sendMessage(
		@DestinationVariable("roomId") Long roomId,
		@Payload MessageDto message
	) {
		log.debug("Received message DTO: {}", message);
		try {
			ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
			Long receiverProfileId = chatRoom.getFromProfileId().equals(message.fromProfileId())
				? chatRoom.getToProfileId()
				: chatRoom.getFromProfileId();

			// 메시지 발행만 수행 (DB 저장 및 WebSocket 브로드캐스트는 Consumer에서 처리)
			messageService.sendMessage(
				message.fromProfileId(),
				receiverProfileId,
				message.content(),
				roomId
			);

		} catch (Exception e) {
			log.error("Message processing failed: ", e);
			messagingTemplate.convertAndSendToUser(
				message.fromProfileId().toString(),
				"/queue/errors",
				new ChatException("MESSAGE_SEND_FAILED", e.getMessage())
			);
		}
	}

	@Override
	@SubscribeMapping("/chat/{roomId}")
	public void subscribeToChat(Long roomId) {
		// Swagger 문서화용 메서드
		log.debug("Chat subscription endpoint documentation for room: {}", roomId);
	}

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();

		if (sessionId != null) {
			connectedSessions.add(sessionId);
			log.info("New WebSocket connection established: {}", sessionId);
		}
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();

		if (sessionId != null && connectedSessions.remove(sessionId)) {
			log.info("User disconnected: {}", sessionId);
		}
	}
}