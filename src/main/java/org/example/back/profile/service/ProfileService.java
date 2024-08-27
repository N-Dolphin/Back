package org.example.back.profile.service;

import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProfileService {

	private final ProfileRepository profileRepository;

	public void createProfile(final ProfileCreateRequest request) {
		final Profile newProfile = request.toProfile();
		profileRepository.save(newProfile);
	}
}
