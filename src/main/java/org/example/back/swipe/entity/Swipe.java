package org.example.back.swipe.entity;

import org.example.back.matching.MatchingEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Swipe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 좋아요를 누른 사람의 프로필 ID
	@Column(nullable = false)
	private Long fromProfileId;

	// 좋아요가 눌린 사람의 프로필 ID
	@Column(nullable = false)
	private Long toProfileId;

	// 좋아요 or 싫어요
	private MatchingEnum matchingEnum;
}
