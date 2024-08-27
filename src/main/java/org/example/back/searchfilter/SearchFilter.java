package org.example.back.searchfilter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SearchFilter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	// 프로필 아이디

	PreferredGenderType preferredGenderType;

	Integer minAge;

	Integer maxAge;

	Integer searchRange;

}
