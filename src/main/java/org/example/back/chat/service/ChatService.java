package org.example.back.chat.service;

import java.util.List;
import java.util.Optional;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.repository.ChatMessageRepository;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final ObjectMapper objectMapper;


	public List<ChatRoom> getChatRooms(Long profileId) {
		return chatRoomRepository.findAllByFromProfileIdOrToProfileId(profileId, profileId);
	}

	public boolean isUserInChatRoom(Long profileId, Long chatRoomId) {
		Optional<ChatRoom> chatRoom = chatRoomRepository.findById(chatRoomId);
		return chatRoom.isPresent() &&
			(chatRoom.get().getFromProfileId().equals(profileId) || chatRoom.get().getToProfileId().equals(profileId));
	}

	public void sendMessage(Long fromProfileId, Long toProfileId, String content) {
		MessageDto messageDto = new MessageDto(fromProfileId,toProfileId,content);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String objectToJSON = objectMapper.writeValueAsString(messageDto);

			// 동적 exchange 및 routingKey 설정
			String dynamicExchangeName = "exchange_" + fromProfileId + "_" + toProfileId;
			String routingKey = "route_" + fromProfileId + "_" + toProfileId;

			Long chatRoomId = chatRoomRepository.findChatRoomIdByProfileIds(fromProfileId, toProfileId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾지 못했습니다."));

			// 메시지 전송
			rabbitTemplate.convertAndSend(dynamicExchangeName, routingKey, objectToJSON);
			System.out.println("채팅방 " + chatRoomId + "로 메시지를 전송했습니다: " + content);

		} catch (JsonProcessingException jpe) {
			System.out.println("메시지 파싱 오류: " + jpe.getMessage());
		}
	}

	@Transactional
	public Long createChatRoom(Long fromProfileId, Long toProfileId) {
		Optional<ChatRoom> existingRoom = chatRoomRepository.findByFromProfileIdAndToProfileId(fromProfileId, toProfileId);
		ChatRoom chatRoom = new ChatRoom();

		if (!existingRoom.isPresent()) {
			// 채팅방 생성
			chatRoom.setFromProfileId(fromProfileId);
			chatRoom.setToProfileId(toProfileId);
			chatRoomRepository.save(chatRoom);
			System.out.println("채팅방 생성 완료: " + chatRoom.getId());

			return chatRoom.getId();
		} else {
			System.out.println("이미 존재하는 채팅방입니다: ");
			return existingRoom.get().getId();
		}

	}

	// 특정 채팅방의 모든 메시지 조회
	public List<ChatMessage> getMessages(Long chatRoomId) {
		return chatMessageRepository.findByChatRoomId(chatRoomId);
	}

	// 메시지 처리 메서드 추가
	public void processReceivedMessage(ChatMessage chatMessage) {
		// 메시지 저장 로직 등 메시지 처리 로직 추가
		chatMessageRepository.save(chatMessage);
		System.out.println(chatMessage.getId());
		System.out.println(chatMessage.getContent());
	}

}
