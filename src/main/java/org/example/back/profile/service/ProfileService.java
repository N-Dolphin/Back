package org.example.back.profile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.back.config.interceptor.JwtInterceptor;
import org.example.back.exception.ClientErrorException;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileDistance;
import org.example.back.profile.domain.ProfileLocation;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProfileService {

	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;
	private final GeometryFactory geometryFactory;


	public ProfileCreateResponse createProfile(final ProfileCreateRequest request, Long userId) {
		final Profile newProfile = request.toProfile();

		// 사용자 ID로 UserEntity 조회
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		// 프로필에 사용자 설정
		newProfile.setUserId(userId);
		profileRepository.save(newProfile);

		user.setProfileId(newProfile.getProfileId());
		userRepository.save(user);

		return ProfileCreateResponse.of(newProfile);
	}



	public Profile updateProfileLocation(Long profileId, double longitude, double latitude) {
		Profile profile = profileRepository.findByProfileId(profileId)
			.orElseThrow(ProfileNotFoundException::new);

		Point location = geometryFactory.createPoint(new Coordinate(latitude, longitude));

		profile.setLocation(new ProfileLocation(location.getY(), location.getX()));
		return profileRepository.save(profile);


	}

	public List<Profile> getProfilesWithinDistance(double longitude, double latitude) {
		return profileRepository.findProfilesWithinDistance(longitude, latitude);
	}


}
