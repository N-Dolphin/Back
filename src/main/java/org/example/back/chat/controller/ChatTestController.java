package org.example.back.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class ChatTestController {

	@GetMapping("/chat-rooms/{roomId}")  // URL 패턴 수정
	public ModelAndView chatRoom(@PathVariable("roomId") Long roomId)
	{
		ModelAndView mav = new ModelAndView("chat-room");
		mav.addObject("roomId", roomId);
		return mav;
	}

}