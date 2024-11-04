package org.example.back.swipe.service;

import org.example.back.chat.service.ChatService;
import org.example.back.rabbitmq.MessageDto;
import org.example.back.rabbitmq.service.ConsumerService;
import org.example.back.rabbitmq.service.DynamicQueueService;
import org.example.back.swipe.dto.MatchingEvent;
import org.example.back.swipe.repository.SwipeRepository;
import org.example.back.swipe.entity.Swipe;
import org.example.back.matching.MatchingEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeService {
	private final SwipeRepository swipeRepository;
	private final RabbitTemplate rabbitTemplate;
	private final ChatService chatService;
	private final RabbitAdmin rabbitAdmin;
	private final DynamicQueueService dynamicQueueService;
	private final ObjectMapper objectMapper;

	@Transactional
	public Swipe swipe(Long fromProfileId, Long toProfileId, MatchingEnum matchingEnum) {
		// 중복 체크
		if (swipeRepository.existsByFromProfileIdAndToProfileId(fromProfileId, toProfileId)) {
			throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
		}

		// 새로운 좋아요 객체 생성
		Swipe swipe = new Swipe();
		swipe.setFromProfileId(fromProfileId);
		swipe.setToProfileId(toProfileId);
		swipe.setMatchingEnum(matchingEnum);
		swipeRepository.save(swipe);

		// 반대 방향의 좋아요 여부 확인
		Optional<Swipe> reverseSwipe = swipeRepository.findByFromProfileIdAndToProfileId(toProfileId, fromProfileId);
		if (reverseSwipe.isPresent() && reverseSwipe.get().getMatchingEnum() == MatchingEnum.LIKE) {
			try {
				// 채팅방 생성
				Long chatRoomId = chatService.createChatRoom(fromProfileId, toProfileId);

				// 매칭 완료 메시지 생성
				MessageDto messageDto = new MessageDto(fromProfileId, toProfileId, "채팅방 생성 완료", LocalDateTime.now());
				String messageJson = objectMapper.writeValueAsString(messageDto);
				log.info("전송할 매칭 이벤트 JSON: {}", messageJson);

				// 큐 리스너 생성
				dynamicQueueService.createQueueAndListener(chatRoomId);

				// 매칭 상태 업데이트
				swipe.setMatchingEnum(MatchingEnum.MATCHED);
				swipeRepository.save(swipe);

				// 매칭 완료 메시지 전송
				chatService.sendMessage(fromProfileId, toProfileId, "채팅방 생성 완료", chatRoomId);
				log.info("성공적으로 매칭 이벤트를 전송했습니다.");

			} catch (JsonProcessingException jpe) {
				log.error("JSON 변환 오류 발생: {}", jpe.getMessage());
				throw new RuntimeException("매칭 이벤트 전송 중 오류 발생", jpe);
			}
		}
		return swipe;
	}

	// 특정 프로필이 좋아요를 누른 목록 조회
	public List<Swipe> getLikesFrom(Long fromProfileId) {
		return swipeRepository.findByFromProfileId(fromProfileId);
	}

	// 특정 프로필이 좋아요를 받은 목록 조회
	public List<Swipe> getLikesTo(Long toProfileId) {
		return swipeRepository.findByToProfileId(toProfileId);
	}
}
