package org.example.back.profileimage.service;


import org.example.back.profile.domain.Profile;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profileimage.entity.ProfileImage;
import org.example.back.profileimage.repository.ProfileImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

	private final ProfileImageRepository profileImageRepository;
	private final ProfileRepository profileRepository;

	@Transactional
	public void saveProfileImage(Long profileId, String imageUrl, Integer imageSize) {
		Profile profile = profileRepository.findByProfileId(profileId).orElseThrow();
		// 프로필 ID로 프로필 객체를 가져오는 코드

		ProfileImage profileImage = ProfileImage.of(profile, imageUrl, imageSize);
		profileImageRepository.save(profileImage);
	}

}
