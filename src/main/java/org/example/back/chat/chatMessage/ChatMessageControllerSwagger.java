package org.example.back.chat.chatMessage;

import java.util.List;

import org.example.back.chat.common.dto.MessageRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "ChatMessage API", description = "채팅 메시지 관련 API")
public interface ChatMessageControllerSwagger {

	@Operation(
		summary = "채팅 메시지 조회",
		description = "특정 채팅방의 메시지를 페이징하여 조회합니다.",
		parameters = {
			@Parameter(name = "chatRoomId", description = "채팅방 ID", required = true),
			@Parameter(name = "page", description = "페이지 번호", required = false),
			@Parameter(name = "size", description = "페이지 크기", required = false)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "메시지 조회 성공",
				content = @Content(schema = @Schema(implementation = MessageRes.class))
			)
		}
	)
	ResponseEntity<List<MessageRes>> getChatMessages(
		@PathVariable Long chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "100") int size
	);

	@Operation(
		summary = "채팅방 나가기",
		description = "채팅방을 나가고 관련 정보를 삭제합니다.",
		parameters = {
			@Parameter(name = "chatRoomId", description = "채팅방 ID", required = true)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "채팅방 나가기 성공"
			),
			@ApiResponse(
				responseCode = "404",
				description = "채팅방을 찾을 수 없음",
				content = @Content
			)
		}
	)
	ResponseEntity<Void> leaveChatRoom(
		@PathVariable Long chatRoomId,
		HttpServletRequest request
	);
}