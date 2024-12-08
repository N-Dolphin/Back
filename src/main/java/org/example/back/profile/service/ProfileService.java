package org.example.back.profile.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.back.exception.ClientErrorException;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.controller.request.ProfileUpdateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileDto;
import org.example.back.profile.domain.ProfileInfoDto;
import org.example.back.profile.domain.ProfileLocation;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.profileimage.entity.ProfileImage;
import org.example.back.profileimage.repository.ProfileImageRepository;
import org.example.back.swipe.repository.SwipeRepository;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.UserRepository;
import org.example.back.user.service.UserService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
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
	private final UserService userService;


	@Transactional
	public ProfileDto createProfile(final ProfileCreateRequest request, Long userId) {
		final Profile newProfile = request.toProfile();

		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		newProfile.setUserId(userId);
		newProfile.setDateOfBirth(LocalDate.parse(request.dateOfBirth()));
		profileRepository.save(newProfile);

		user.setProfileId(newProfile.getProfileId());
		userRepository.save(user);

		String url= String.valueOf(profileImageRepository.findFirstByProfile_ProfileId(newProfile.getProfileId()));

		return ProfileDto.of(newProfile,url);
	}


	@Transactional
	public ProfileDto updateProfile(final ProfileUpdateRequest request, Long userId) {

		Long profileId= userService.getProfileIdByUserId(userId);
		Profile newProfile= profileRepository.findByProfileId(profileId).orElseThrow(
			ProfileNotFoundException::new
		);

		// 프로필 수정
		newProfile.setProfileName(request.profileName());
		newProfile.setSelfIntroduce(request.selfIntroduction());
		profileRepository.save(newProfile);


		String url= String.valueOf(profileImageRepository.findFirstByProfile_ProfileId(newProfile.getProfileId()));

		return ProfileDto.of(newProfile,url);
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
		// List<Profile> profilesList = profileRepository.findProfilesSortedByDistance(currentLocation, profileId, 3000);

		List<Profile> profilesList = profileRepository.findUnswipedProfilesSortedByDistance(
			currentLocation,
			profileId,
			3000
		);

		// 각 프로필에 대한 첫 번째 이미지를 함께 조회하여 ProfileDto로 변환
		List<ProfileDto> profileDtos = profilesList.stream()
			.map(p -> {
				Optional<ProfileImage> firstImage = profileImageRepository.findFirstByProfile_ProfileId(p.getProfileId());
				return new ProfileDto(firstImage.get().getImageUrl(), p.getProfileName(), p.getAge());
			})
			.collect(Collectors.toList());

		return profileDtos;
	}


	public ProfileInfoDto getProfileInfo(Long profileId) {
		// 해당 프로필 찾기
		Profile profile = profileRepository.findByProfileId(profileId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid profile ID"));

		ProfileImage firstImage = profileImageRepository.findFirstByProfile_ProfileId(profileId).orElseThrow(
			()-> new ClientErrorException(HttpStatus.NOT_FOUND,"이미지가 없어요")
		);

		return new ProfileInfoDto(firstImage.getImageUrl(),profile.getProfileName(),profile.getAge());
	}

	public Long findProfileByNickname(String toProfileName) {

		Profile profile= profileRepository.findProfileByProfileName(toProfileName).orElseThrow(
			ProfileNotFoundException::new
		);

		return profile.getProfileId();
	}

	public Profile findProfileByProfileId(Long profileId) {
		return profileRepository.findByProfileId(profileId).orElseThrow(
			ProfileNotFoundException::new
		);
	}





}
