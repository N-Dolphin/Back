package org.example.back.chat.chatMessage;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.example.back.chat.chatRoom.ChatRoom;
import org.example.back.chat.chatRoom.ChatRoomRepository;
import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatRoomEnterRequest;
import org.example.back.chat.common.dto.EnterConfirmRes;
import org.example.back.chat.common.dto.MessageRes;
import org.example.back.chat.util.StompHeaderAccessorUtil;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageServiceImpl chatMessageService;
	private final StompHeaderAccessorUtil stompHeaderAccessorUtil;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomRepository chatRoomRepository;


	// Destination Queue: /pub/chat.message를 통해 호출 후 처리 되는 로직

	@MessageMapping("chat.message")
	public void sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq message) {
		try {
			// 서비스에서 메시지 처리 및 응답 받기
			MessageRes messageRes = chatMessageService.sendMessage(accessor, message);
			System.out.println("Message sent successfully: " + messageRes);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to send message: " + e.getMessage());
		}
	}

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		chatMessageService.handleInitialConnect(accessor);
	}

	@MessageMapping("chat.enter")
	public void  enterChatRoom(StompHeaderAccessor accessor, ChatRoomEnterRequest request) {
		try {
			Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor); // 접속한 사용자의 Profile ID
			Long chatRoomId = request.getChatRoomId();

			// 채팅방 ID를 세션에 저장
			stompHeaderAccessorUtil.setChatRoomIdInSession(accessor, chatRoomId);

			// 채팅방 조회 및 참가자 구분
			ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
				.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

			List<Long> participantIds = chatRoom.getParticipants().stream()
				.map(ChatRoomParticipant::getProfileId)
				.sorted()
				.toList();

			if (participantIds.size() != 2) {
				throw new RuntimeException("잘못된 채팅방 구성입니다.");
			}

			// 방장(RoomMaker)과 게스트(Guest) 정보 생성
			ChatDto.ChatRoomCreateRes roomInfo = ChatDto.ChatRoomCreateRes.createRes(
				chatRoom.getId(),
				participantIds.get(0),
				participantIds.get(1)
			);

			// roomInfo로 참가자 정보 전송
			messagingTemplate.convertAndSend(
				"/exchange/chat.exchange/room." + chatRoomId,
				roomInfo
			);

			// 입장 확인 메시지는 단순하게 전송
			messagingTemplate.convertAndSend(
				"/exchange/chat.exchange/room." + chatRoomId,
				new EnterConfirmRes(profileId, chatRoomId)
			);

			System.out.println("Chat room entered - Room ID: " + chatRoomId + ", Profile ID: " + profileId);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		chatMessageService.handleDisconnectMessage(accessor);
	}

	@GetMapping("/chat-messages/chat-rooms/{chatRoomId}")
	public ResponseEntity getChatMessages(@PathVariable("chatRoomId") Long chatRoomId) {
		List<MessageRes> chatMessageResList = chatMessageService.getChatMessages(chatRoomId);
		return ResponseEntity.ok(chatMessageResList);
	}
}




