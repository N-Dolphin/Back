package org.example.back.swipe.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import org.example.back.swipe.entity.Swipe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Swipe API", description = "스와이프 관련 API")
public interface SwipeControllerSwagger {

	@Operation(
		summary = "좋아요 API",
		description = "특정 유저에게 좋아요를 보냅니다.",
		parameters = @Parameter(name = "toUsername", description = "좋아요를 보낼 유저의 닉네임", required = true),
		responses = {
			@ApiResponse(responseCode = "200", description = "좋아요 성공", content = @Content(schema = @Schema(implementation = Swipe.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
			@ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
		}
	)
	ResponseEntity<Swipe> like(@RequestParam String toUsername, HttpServletRequest request);

	@Operation(
		summary = "싫어요 API",
		description = "특정 유저에게 싫어요를 보냅니다.",
		parameters = @Parameter(name = "toUsername", description = "싫어요를 보낼 유저의 닉네임", required = true),
		responses = {
			@ApiResponse(responseCode = "200", description = "싫어요 성공", content = @Content(schema = @Schema(implementation = Swipe.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
			@ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
		}
	)
	ResponseEntity<Swipe> dislike(@RequestParam String toUsername, HttpServletRequest request);

	@Operation(
		summary = "좋아요를 누른 목록 조회",
		description = "해당 프로필이 보낸 좋아요 목록을 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(type = "array", implementation = Swipe.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
		}
	)
	ResponseEntity<List<Swipe>> getLikesFrom(Long profileId);

	@Operation(
		summary = "좋아요를 받은 목록 조회",
		description = "해당 프로필이 받은 좋아요 목록을 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(type = "array", implementation = Swipe.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
		}
	)
	ResponseEntity<List<Swipe>> getLikesTo(Long profileId);
}
