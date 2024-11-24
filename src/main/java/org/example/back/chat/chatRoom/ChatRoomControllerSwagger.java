package org.example.back.chat.chatRoom;

import java.util.List;

import org.example.back.chat.common.dto.ChatRoomParticipantsRecord;
import org.example.back.chat.common.dto.ChatRoomRes;
import org.example.back.chat.common.dto.SimpleChatRoomRecord;
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

@Tag(name = "ChatRoom API", description = "채팅방 관련 API")
public interface ChatRoomControllerSwagger {

	@Operation(
		summary = "간단한 채팅방 목록 조회",
		description = "JWT 토큰을 기반으로 사용자의 간단한 채팅방 정보 목록을 조회합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "채팅방 목록 조회 성공",
				content = @Content(schema = @Schema(
					implementation = SimpleChatRoomRecord.class,
					type = "array"
				))
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
		summary = "채팅방 참여자 조회",
		description = "채팅방 ID를 기반으로 해당 채팅방의 참여자 목록을 조회합니다.",
		parameters = {
			@Parameter(
				name = "roomId",
				description = "채팅방 ID",
				required = true,
				schema = @Schema(type = "integer", format = "int64")
			)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "채팅방 참여자 조회 성공",
				content = @Content(schema = @Schema(implementation = ChatRoomParticipantsRecord.class))
			),
			@ApiResponse(
				responseCode = "404",
				description = "채팅방을 찾을 수 없음",
				content = @Content
			)
		}
	)
	ResponseEntity<ChatRoomParticipantsRecord> getChatRoomParticipants(@PathVariable("roomId") Long roomId);
}