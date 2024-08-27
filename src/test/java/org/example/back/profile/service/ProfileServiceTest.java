package org.example.back.profile.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.type.Gender;
import org.example.back.profile.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProfileServiceTest {

	@Autowired
	private ProfileService profileService;

	@Autowired
	private ProfileRepository profileRepository;

	@DisplayName("새로운 프로필을 생성하고 저장한다.")
	@Test
	void createProfile() {
		ProfileCreateRequest request = new ProfileCreateRequest(
			"피카츄", "안녕하세요 피카츄입니다.", 23, Gender.MALE
		);

		profileService.createProfile(request);

		assertThat(profileRepository.count()).isEqualTo(1);
	}
}