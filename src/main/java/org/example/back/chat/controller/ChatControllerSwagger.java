package org.example.back.chat.controller;

import org.example.back.chat.dto.ChatRoomDto;
import org.example.back.chat.dto.MessageResponseDto;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "Chat API", description = "채팅 관련 API")
public interface ChatControllerSwagger {

	@Operation(
		summary = "채팅방 목록 조회",
		description = "사용자가 속한 모든 채팅방 목록을 조회합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "채팅방 목록 반환 성공",
				content = @Content(schema = @Schema(implementation = ChatRoom.class))
			)
		}
	)
	ResponseEntity<List<ChatRoomDto>> getChatRooms(HttpServletRequest request);

	@Operation(
		summary = "채팅방 내 메시지 조회",
		description = "특정 채팅방 내 메시지를 조회합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "메시지 목록 반환 성공",
				content = @Content(schema = @Schema(implementation = MessageResponseDto.class))
			),
			@ApiResponse(
				responseCode = "403",
				description = "사용자가 채팅방에 속하지 않은 경우",
				content = @Content
			)
		}
	)
	ResponseEntity<MessageResponseDto> getMessages(
		@PathVariable Long chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		HttpServletRequest request
	);


	@Operation(
		summary = "메시지 전송",
		description = "특정 채팅방에 메시지를 전송합니다.",
		requestBody = @RequestBody(content =
		@Content(schema = @Schema(implementation = MessageDto.class))),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "메시지 전송 성공"
			),
			@ApiResponse(
				responseCode = "403",
				description = "사용자가 채팅방에 속하지 않은 경우",
				content = @Content
			)
		}
	)
	ResponseEntity<String> sendMessage(Long chatRoomId, MessageDto messageDto, HttpServletRequest request);
}
