package org.example.back.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Slf4j
public class ChatTestController {

	@Value("${server.serverAddress}")
	private String serverAddress;

	@Value("${server.port}")
	private String serverPort;

	@GetMapping("/chat-rooms/{roomId}")  // URL 패턴 수정
	public ModelAndView chatRoom(@PathVariable("roomId") Long roomId)
	{
		ModelAndView mav = new ModelAndView("chat-room");
		mav.addObject("roomId", roomId);
		return mav;
	}

	// 서버 설정을 제공하는 엔드포인트
	@GetMapping("/config")
	@ResponseBody  // REST 응답을 위해 필요
	public Map<String, String> getConfig() {
		Map<String, String> config = new HashMap<>();
		config.put("serverAddress", serverAddress);
		config.put("serverPort", serverPort);
		log.debug("Providing server config: address={}, port={}", serverAddress, serverPort);
		return config;
	}
}



