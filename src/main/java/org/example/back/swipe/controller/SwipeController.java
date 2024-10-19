package org.example.back.swipe.controller;


import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.profile.service.ProfileService;
import org.example.back.swipe.entity.Swipe;
import org.example.back.swipe.service.SwipeService;
import org.example.back.matching.MatchingEnum;
import org.example.back.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/swipes")
@RequiredArgsConstructor
public class SwipeController implements SwipeControllerSwagger{

	private final SwipeService swipeService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final ProfileService profileService;

	// 좋아요 API (fromProfileId -> toProfileId)
	@PostMapping("/like")
	@Override
	public ResponseEntity<Swipe> like(
		@RequestParam String toProfileName,
		HttpServletRequest request
	) {

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long fromUserId = Long.valueOf(userIdToken);
		Long fromProfileId = userService.getProfileIdByUserId(fromUserId);

		Long toProfileId= profileService.findProfileByNickname(toProfileName);

		Swipe swipe = swipeService.swipe(fromProfileId, toProfileId, MatchingEnum.LIKE);
		return ResponseEntity.ok(swipe);
	}

	// 싫어요 API
	@PostMapping("/dislike")
	@Override
	public ResponseEntity<Swipe> dislike(
		@RequestParam String toProfileName,
		HttpServletRequest request
	) {

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long fromUserId = Long.valueOf(userIdToken);
		Long fromProfileId = userService.getProfileIdByUserId(fromUserId);

		Long toProfileId= profileService.findProfileByNickname(toProfileName);

		Swipe swipe = swipeService.swipe(fromProfileId, toProfileId, MatchingEnum.DISLIKE);
		return ResponseEntity.ok(swipe);
	}

	// 좋아요를 누른 목록 조회
	@GetMapping("/likes/from/{profileId}")
	@Override
	public ResponseEntity<List<Swipe>> getLikesFrom(@PathVariable Long profileId) {
		List<Swipe> likes = swipeService.getLikesFrom(profileId);
		return ResponseEntity.ok(likes);
	}

	// 좋아요를 받은 목록 조회
	@GetMapping("/likes/to/{profileId}")
	@Override
	public ResponseEntity<List<Swipe>> getLikesTo(@PathVariable Long profileId) {
		List<Swipe> likes = swipeService.getLikesTo(profileId);
		return ResponseEntity.ok(likes);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
