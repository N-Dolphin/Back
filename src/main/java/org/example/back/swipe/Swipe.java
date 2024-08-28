package org.example.back.swipe;

import org.example.back.matching.MatchingEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Swipe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	// 유저 1

	// 유저 2

	//좋아요 or 싫어요
	MatchingEnum matchingEnum;
}
