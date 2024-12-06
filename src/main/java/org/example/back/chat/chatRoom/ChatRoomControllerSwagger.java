package org.example.back.chat.chatRoom;

import java.util.List;

import org.example.back.chat.common.dto.ChatRoomAccessResponse;
import org.example.back.chat.common.dto.ChatRoomParticipantsRecord;
import org.example.back.chat.common.dto.SimpleChatRoomRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "ChatRoom API", description = "채팅방 관련 API")
public interface ChatRoomControllerSwagger {

	@Operation(
		summary = "채팅방 목록 조회",
		description = "사용자의 채팅방 목록을 조회합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "채팅방 목록 조회 성공",
				content = @Content(schema = @Schema(implementation = SimpleChatRoomRecord.class))
			),
			@ApiResponse(
				responseCode = "401",
				description = "인증 실패",
				content = @Content
			)
		}
	)
	ResponseEntity<List<SimpleChatRoomRecord>> getSimpleChatRooms(HttpServletRequest request);

	@Operation(
		summary = "채팅방 참가자 조회",
		description = "특정 채팅방의 참가자 목록을 조회합니다.",
		parameters = {
			@Parameter(name = "roomId", description = "채팅방 ID", required = true)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "참가자 목록 조회 성공",
				content = @Content(schema = @Schema(implementation = ChatRoomParticipantsRecord.class))
			)
		}
	)
	ResponseEntity<ChatRoomParticipantsRecord> getChatRoomParticipants(@PathVariable Long roomId);

	@Operation(
		summary = "채팅방 접근 권한 검증",
		description = "특정 채팅방에 대한 사용자의 접근 권한을 검증합니다.",
		parameters = {
			@Parameter(name = "chatRoomId", description = "채팅방 ID", required = true)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "접근 권한 검증 성공",
				content = @Content(schema = @Schema(implementation = ChatRoomAccessResponse.class))
			),
			@ApiResponse(
				responseCode = "403",
				description = "접근 권한 없음",
				content = @Content
			)
		}
	)
	ResponseEntity<ChatRoomAccessResponse> validateAccess(
		@PathVariable Long chatRoomId,
		HttpServletRequest request
	);
}