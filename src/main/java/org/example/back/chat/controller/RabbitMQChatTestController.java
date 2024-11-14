package org.example.back.chat.controller;

import java.util.List;

import org.example.back.chat.dto.ChatRoomDto;
import org.example.back.chat.service.ChatRoomService;
import org.example.back.matching.MatchingEnum;
import org.example.back.swipe.service.SwipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;
@Controller
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class RabbitMQChatTestController {

	private final SwipeService swipeService;
	private final ChatRoomService chatRoomService;

	@GetMapping("/match")
	public ModelAndView matchTest() {
		ModelAndView mav = new ModelAndView("match-test");
		return mav;
	}

	@PostMapping("/match")
	@ResponseBody
	public ResponseEntity<?> executeMatch(
		@RequestParam("fromProfileId") Long fromProfileId,  // 매개변수 이름 명시
		@RequestParam("toProfileId") Long toProfileId) {    // 매개변수 이름 명시
		try {
			swipeService.swipe(fromProfileId, toProfileId, MatchingEnum.LIKE);
			swipeService.swipe(toProfileId, fromProfileId, MatchingEnum.LIKE);
			return ResponseEntity.ok("Matching successful");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/rooms/{profileId}")
	@ResponseBody
	public ResponseEntity<?> getChatRooms(@PathVariable("profileId") Long profileId) {  // 변수 이름 명시
		List<ChatRoomDto> rooms = chatRoomService.getChatRooms(profileId);
		return ResponseEntity.ok(rooms);
	}
}