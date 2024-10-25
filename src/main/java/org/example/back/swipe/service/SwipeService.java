package org.example.back.swipe.service;

import org.example.back.chatroom.service.ChatRoomService;
import org.example.back.swipe.repository.SwipeRepository;
import org.example.back.swipe.entity.Swipe;
import org.example.back.matching.MatchingEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwipeService {

	private final SwipeRepository swipeRepository;
	private final ChatRoomService chatRoomService;


	// 좋아요 처리 (fromProfileId가 toProfileId에 대해 좋아요)
	@Transactional
	public Swipe swipe(Long fromProfileId, Long toProfileId, MatchingEnum matchingEnum) {
		// 중복 체크 (이미 좋아요를 눌렀는지 확인)
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
			// 서로 좋아요가 존재하므로 매칭 성사 -> 채팅방 생성
			chatRoomService.createChatRoom(fromProfileId, toProfileId);
			swipe.setMatchingEnum(MatchingEnum.MATCHED);
		}
		//싫어요 시 로직은 후에 처리


		// 좋아요 또는 매칭 상태로 저장
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
