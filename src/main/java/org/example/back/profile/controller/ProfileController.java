package org.example.back.profile.controller;

import java.util.List;
import java.util.Optional;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.exception.ClientErrorException;
import org.example.back.location.LocationRequest;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileDistance;
import org.example.back.profile.domain.ProfileDto;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.ProfileService;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.profileimage.entity.ProfileImage;
import org.example.back.profileimage.repository.ProfileImageRepository;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.InvalidTokenException;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.UserRepository;
import org.example.back.user.service.UserService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class ProfileController implements ProfileControllerSwagger {

	private final ProfileService profileService;
	private final JwtTokenProvider jwtTokenProvider;
	private final ProfileRepository profileRepository;
	private final UserService userService;
	private final ProfileImageRepository profileImageRepository;

	@PostMapping
	@Override
	public ResponseEntity<ProfileCreateResponse> createProfile(@Valid @RequestBody final ProfileCreateRequest request,
		HttpServletRequest httpServletRequest) {

			String userId = (String) httpServletRequest.getAttribute("userId");

			if (profileRepository.findByUserId(Long.valueOf(userId)).isPresent()){
				System.out.println(userId);
				throw  new ClientErrorException(HttpStatus.CONFLICT,"이미 프로필이 존재합니다");
			}
			// 사용자 ID를 기반으로 프로필 생성 로직 수행
			ProfileCreateResponse response = profileService.createProfile(request, Long.valueOf(userId));
			httpServletRequest.setAttribute("profileId", String.valueOf(response.id()));

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/saveLocation")
	@Override
	public ResponseEntity<ProfileDto> saveLocation(@RequestBody LocationRequest locationRequest, HttpServletRequest httpServletRequest) {

		String token = resolveToken(httpServletRequest);

		// JWT에서 userId 추출
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId= Long.valueOf(userIdToken);
		Long profileId= userService.getProfileIdByUserId(userId);
		Profile profile = profileRepository.findByProfileId(profileId).orElseThrow(
			ProfileNotFoundException::new
		);

		ProfileImage image= profileImageRepository.findFirstByProfile_ProfileId(profileId);

		profileService.updateProfileLocation(profileId, locationRequest.longitude(),
			locationRequest.latitude());

		return ResponseEntity.ok(new ProfileDto(image.getImageUrl(),profile.getProfileName(),profile.getAge()));
	}




	@GetMapping("/findProfiles")
	@Override
	public ResponseEntity<List<ProfileDto>> findProfiles(HttpServletRequest request) {

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);

		Long profileId = userService.getProfileIdByUserId(userId);

		List<ProfileDto> profiles = profileService.getProfiles(profileId);

		return ResponseEntity.ok(profiles);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}


}
