package org.example.back.rabbitmq.controller;

import org.example.back.rabbitmq.ApiResponse;
import org.example.back.rabbitmq.MessageDto;
import org.example.back.rabbitmq.SuccessCode;
import org.example.back.rabbitmq.service.ProducerService;
import org.example.back.rabbitmq.service.ProducerServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/producer")
@RequiredArgsConstructor
public class ProducerController {

	private final ProducerServiceImpl producerService;

	@PostMapping("/send")
	public ResponseEntity<?> sendMessage(@RequestBody MessageDto messageDto) {
		String result = "";

		producerService.sendMessage(messageDto);
		ApiResponse ar = new ApiResponse(
			result,
			SuccessCode.SELECT.getStatus(),
			SuccessCode.SELECT.getMessage()
		);
		return new ResponseEntity<>(ar, HttpStatus.OK);
	}
}