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

import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SwipeService {

	private final SwipeRepository swipeRepository;
	private final RabbitTemplate rabbitTemplate;
	private final ChatService chatService;
	private final RabbitAdmin rabbitAdmin;
	private final ConsumerService consumerService;
	private final DynamicQueueService dynamicQueueService;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;


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
				// MatchingEvent 객체를 JSON 문자열로 변환
				ObjectMapper objectMapper = new ObjectMapper();

				MessageDto messageDto= new MessageDto(fromProfileId,toProfileId,"채팅방 생성 완료");

				String objectToJSON = objectMapper.writeValueAsString(messageDto);
				System.out.println("전송할 매칭 이벤트 JSON: " + objectToJSON);

				// 채팅방 생성
				Long chatRoomId= chatService.createChatRoom(fromProfileId, toProfileId);
				dynamicQueueService.createQueueAndListener(fromProfileId, toProfileId, chatRoomId);

				swipe.setMatchingEnum(MatchingEnum.MATCHED);
				swipeRepository.save(swipe);


				// exchange 및 queue 이름을 프로필 ID 조합으로 동적으로 생성
				String ExchangeName = "exchange_" + fromProfileId + "_" + toProfileId;
				String queueName = "chat_room_" + chatRoomId; // 큐 이름 정의
				String routingKey = "route_" + fromProfileId + "_" + toProfileId;

				// Exchange 및 Queue 선언 및 바인딩
				DirectExchange exchange = new DirectExchange(ExchangeName);
				Queue queue = new Queue(queueName, true); // durable 큐 생성

				rabbitAdmin.declareExchange(exchange);
				rabbitAdmin.declareQueue(queue);
				Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
				rabbitAdmin.declareBinding(binding);


				System.out.println("채팅방 " + chatRoomId + "에 대한 큐 " + queueName + "가 생성되었습니다.");
				rabbitTemplate.convertAndSend(exchangeName, routingKey, objectToJSON);
				System.out.println("성공적으로 매칭 이벤트를 전송했습니다.");


			} catch (JsonProcessingException jpe) {
				System.out.println("JSON 변환 오류 발생: " + jpe.getMessage());
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
