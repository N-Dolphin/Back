package org.example.back.chat.config;

import java.time.LocalDateTime;
import java.util.List;

import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.rabbitmq.service.DynamicQueueService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomCleanupScheduler {
	private final ChatRoomRepository chatRoomRepository;
	private final DynamicQueueService dynamicQueueService;

	@Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시에 실행
	public void cleanupInactiveChatRooms() {
		LocalDateTime threshold = LocalDateTime.now().minusDays(30);
		// 수정된 메소드 이름으로 호출
		List<ChatRoom> inactiveRooms = chatRoomRepository.findByLastActivityBeforeAndIsActiveTrue(threshold);

		for (ChatRoom room : inactiveRooms) {
			dynamicQueueService.removeQueueAndListener(room.getId());
			room.setActive(false);
			chatRoomRepository.save(room);
		}
	}
}