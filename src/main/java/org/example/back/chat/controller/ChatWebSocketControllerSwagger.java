package org.example.back.chat.controller;

import org.example.back.chat.exception.ChatException;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;



@Tag(name = "WebSocket Chat API", description = "웹소켓 채팅 관련 API")
@RequestMapping("/api/v1/websocket")  // Swagger UI에 표시하기 위한 기본 경로
public interface ChatWebSocketControllerSwagger {

	@Operation(
		summary = "채팅방 웹소켓 연결",
		description = """
            웹소켓 연결을 위한 엔드포인트입니다.
            
            - 연결 URL: /api/v1/ws-chat
            - 필수 헤더: Authorization: Bearer {jwt_token}
            
            ```javascript
            const socket = new SockJS('/api/v1/ws-chat');
            const stompClient = Stomp.over(socket);
            stompClient.connect({ 'Authorization': 'Bearer ' + token });
            ```
            """
	)
	@GetMapping("/docs/connect")  // Swagger UI에 표시하기 위한 가상 엔드포인트
	void connectWebSocket();

	@Operation(
		summary = "채팅 메시지 전송",
		description = """
            STOMP를 통해 메시지를 전송합니다.
            
            - 전송 주소: /app/chat/{roomId}/sendMessage
            - 메시지 형식: MessageDto
            
            ```javascript
            stompClient.send('/app/chat/' + roomId + '/sendMessage', {}, 
                JSON.stringify({
                    content: '메시지 내용',
                    chatRoomId: roomId,
                    timestamp: new Date()
                })
            );
            ```
            """,
		parameters = {
			@Parameter(name = "roomId", description = "채팅방 ID", required = true)
		},
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @Content(schema = @Schema(implementation = MessageDto.class))
		)
	)
	@MessageMapping("/chat/{roomId}/sendMessage")
	void sendMessage(@PathVariable Long roomId, @Payload MessageDto message);

	@Operation(
		summary = "채팅방 구독",
		description = """
            특정 채팅방의 메시지를 구독합니다.
            
            - 구독 주소: /topic/chat/{roomId}
            
            ```javascript
            stompClient.subscribe('/topic/chat/' + roomId, function(message) {
                const data = JSON.parse(message.body);
                // 메시지 처리
            });
            ```
            """,
		parameters = {
			@Parameter(name = "roomId", description = "구독할 채팅방 ID", required = true)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "구독 성공",
				content = @Content(schema = @Schema(implementation = MessageDto.class))
			)
		}
	)
	@SubscribeMapping("/chat/{roomId}")
	void subscribeToChat(@PathVariable Long roomId);
}