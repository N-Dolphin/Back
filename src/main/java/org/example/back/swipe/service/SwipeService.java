package org.example.back.swipe.service;

import org.example.back.swipe.repository.SwipeRepository;
import org.example.back.swipe.entity.Swipe;
import org.example.back.matching.MatchingEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SwipeService {

	private final SwipeRepository swipeRepository;

	// 좋아요 처리 (fromProfileId가 toProfileId에 대해 좋아요)
	@Transactional
	public Swipe swipe(Long fromProfileId, Long toProfileId, MatchingEnum matchingEnum) {
		// 중복 체크 (이미 좋아요를 눌렀는지 확인)
		if (swipeRepository.existsByFromProfileIdAndToProfileId(fromProfileId, toProfileId)) {
			throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
		}

		Swipe swipe = new Swipe();
		swipe.setFromProfileId(fromProfileId);
		swipe.setToProfileId(toProfileId);
		swipe.setMatchingEnum(matchingEnum);
		return swipeRepository.save(swipe);
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
