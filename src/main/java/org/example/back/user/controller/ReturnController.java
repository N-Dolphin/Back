package org.example.back.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReturnController {

	@GetMapping("/oauth2/kakao/callback")
	public @ResponseBody String kakaoCallback(){
		return  "카카오 인증 완료";
	}
}
