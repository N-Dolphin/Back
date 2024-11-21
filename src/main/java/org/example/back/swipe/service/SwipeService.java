package org.example.back.swipe.service;


import org.example.back.chat.chatRoom.ChatRoomService;
import org.example.back.chat.chatRoom.ChatRoomServiceImpl;
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
	private final ChatRoomServiceImpl chatRoomService;

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
				// 매칭 성공 시 채팅방 생성만 호출
				chatRoomService.createMatchedChatRoom(fromProfileId, toProfileId);
				swipe.setMatchingEnum(MatchingEnum.MATCHED);
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
