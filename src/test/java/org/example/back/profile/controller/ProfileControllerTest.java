package org.example.back.profile.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

import org.example.back.exception.ClientErrorException;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.type.Gender;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.ProfileService;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

class ProfileControllerTest {

	@InjectMocks
	private ProfileController profileController;

	@Mock
	private ProfileService profileService;

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private HttpServletRequest httpServletRequest;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createProfile_Success() {
		// Arrange
		String userId = "1";
		ProfileCreateRequest request = new ProfileCreateRequest("피카츄", "안녕하세요", 23, Gender.MALE);
		ProfileCreateResponse response = new ProfileCreateResponse(1L, "피카츄", 23, "안녕하세요", Gender.MALE);

		when(httpServletRequest.getAttribute("userId")).thenReturn(userId);
		when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
		when(profileService.createProfile(any(), anyLong())).thenReturn(response);

		// Act
		ResponseEntity<ProfileCreateResponse> result = profileController.createProfile(request, httpServletRequest);

		// Assert
		verify(profileRepository).findByUserId(Long.valueOf(userId));
		verify(profileService).createProfile(request, Long.valueOf(userId));
		verify(httpServletRequest).setAttribute("profileId", String.valueOf(response.id()));
		assertEquals(CREATED, result.getStatusCode());
		assertEquals(response, result.getBody());
	}

	@Test
	void createProfile_ProfileAlreadyExists() {
		// Arrange
		String userId = "1";
		Profile existingProfile = new Profile(); // Create a profile object as needed
		when(httpServletRequest.getAttribute("userId")).thenReturn(userId);
		when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(existingProfile));

		// Act & Assert
		ClientErrorException exception = assertThrows(ClientErrorException.class, () -> {
			profileController.createProfile(new ProfileCreateRequest("피카츄", "안녕하세요", 23, Gender.MALE), httpServletRequest);
		});

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("이미 프로필이 존재합니다", exception.getMessage());
	}
}
