package org.example.back.swipe.repository;


import org.example.back.swipe.entity.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {

	// 특정 프로필이 좋아요를 누른 목록 조회
	List<Swipe> findByFromProfileId(Long fromProfileId);

	// 특정 프로필이 좋아요를 받은 목록 조회
	List<Swipe> findByToProfileId(Long toProfileId);

	// 특정 프로필 간 좋아요 여부 확인 (중복 방지)
	boolean existsByFromProfileIdAndToProfileId(Long fromProfileId, Long toProfileId);

	Optional<Swipe> findByFromProfileIdAndToProfileId(Long toProfileId, Long fromProfileId);
}
