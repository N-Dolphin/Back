package org.example.back.profile.service;

import java.util.List;
import java.util.stream.Collectors;

import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileDto;
import org.example.back.profile.domain.ProfileLocation;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.profileimage.entity.ProfileImage;
import org.example.back.profileimage.repository.ProfileImageRepository;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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
	private final ProfileImageRepository profileImageRepository;


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


	public List<ProfileDto> getProfiles(Long profileId) {
		// 해당 프로필 찾기
		Profile profile = profileRepository.findByProfileId(profileId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid profile ID"));

		// 현재 위치 정보 가져오기
		Point currentLocation = profile.getLocation().getLocation();

		// 반경 3km 이내의 프로필을 찾고, 본인 제외, 거리 순으로 정렬
		List<Profile> profilesList = profileRepository.findProfilesSortedByDistance(currentLocation, profileId, 3000);

		// 각 프로필에 대한 첫 번째 이미지를 함께 조회하여 ProfileDto로 변환
		List<ProfileDto> profileDtos = profilesList.stream()
			.map(p -> {
				ProfileImage firstImage = profileImageRepository.findFirstByProfile_ProfileId(p.getProfileId());
				return new ProfileDto(firstImage.getImageUrl(), p.getProfileName(), p.getAge());
			})
			.collect(Collectors.toList());

		return profileDtos;
	}

	public Long findProfileByNickname(String toUsername) {

		Profile profile= profileRepository.findProfileByNickname(toUsername);

		return profile.getProfileId();
	}
}
