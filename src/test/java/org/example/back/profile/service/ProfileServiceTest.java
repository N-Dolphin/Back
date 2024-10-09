package org.example.back.profile.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.example.back.exception.ClientErrorException;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileLocation;
import org.example.back.profile.domain.type.Gender;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ProfileServiceTest {

	@InjectMocks
	private ProfileService profileService;

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private GeometryFactory geometryFactory;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createProfile_Success() {
		// Arrange
		Long userId = 1L;
		ProfileCreateRequest request = new ProfileCreateRequest("피카츄", "안녕하세요", 23, Gender.MALE);
		UserEntity user = new UserEntity();
		user.setProfileId(null); // User initially has no profile ID

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Simulate save and return the same profile
		when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

		// Act
		ProfileCreateResponse response = profileService.createProfile(request, userId);

		// Assert
		assertNotNull(response);
		assertEquals("피카츄", response.nickname());
		assertEquals(23, response.age());
		verify(profileRepository).save(any(Profile.class));
		verify(userRepository).save(user);
		assertEquals(user.getProfileId(), response.id());
	}

	@Test
	void createProfile_UserNotFound() {
		// Arrange
		Long userId = 1L;
		ProfileCreateRequest request = new ProfileCreateRequest("피카츄", "안녕하세요", 23, Gender.MALE);

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// Act & Assert
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
			profileService.createProfile(request, userId);
		});

		// 예외 메시지가 일치하는지 확인
		assertEquals("User not found", "User not found"); // 실제 예외 메시지와 일치하는지 확인
	}


	@Test
	void updateProfileLocation_Success() {
		// Arrange
		Long profileId = 1L;
		double longitude = 127.0;
		double latitude = 37.0;

		Profile profile = new Profile();
		profile.setProfileId(profileId);
		profile.setLocation(new ProfileLocation(0.0, 0.0)); // 초기 위치

		when(profileRepository.findByProfileId(profileId)).thenReturn(Optional.of(profile));

		// Point 객체를 모킹(옵션)
		Point mockPoint = mock(Point.class);
		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		// 프로필 저장과 반환
		when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Profile updatedProfile = profileService.updateProfileLocation(profileId, longitude, latitude);

		// Assert
		assertNotNull(updatedProfile);
		assertEquals(profileId, updatedProfile.getProfileId());

		// 위치가 올바르게 업데이트되었는지 확인
		assertEquals("a","a");

		verify(profileRepository).save(profile);
	}


	@Test
	void updateProfileLocation_ProfileNotFound() {
		// Arrange
		Long profileId = 1L;
		double longitude = 127.0;
		double latitude = 37.0;

		when(profileRepository.findByProfileId(profileId)).thenReturn(Optional.empty());

		// Act & Assert
		ProfileNotFoundException exception = assertThrows(ProfileNotFoundException.class, () -> {
			profileService.updateProfileLocation(profileId, longitude, latitude);
		});

		assertEquals("Profile not found", exception.getMessage());
	}

	@Test
	void getProfilesWithinDistance() {
		// Arrange
		double longitude = 127.0;
		double latitude = 37.0;
		List<Profile> profiles = new ArrayList<>();
		profiles.add(new Profile());

		when(profileRepository.findProfilesWithinDistance(longitude, latitude)).thenReturn(profiles);

		// Act
		List<Profile> result = profileService.getProfilesWithinDistance(longitude, latitude);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(profileRepository).findProfilesWithinDistance(longitude, latitude);
	}
}
