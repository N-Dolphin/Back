package org.example.back.profile.controller;

import java.util.List;
import java.util.Optional;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.exception.ClientErrorException;
import org.example.back.location.LocationRequest;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.controller.request.ProfileUpdateRequest;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileDistance;
import org.example.back.profile.domain.ProfileDto;
import org.example.back.profile.domain.ProfileInfoDto;
import org.example.back.profile.domain.ProfileResponseDto;
import org.example.back.profile.exception.BadRequestException;
import org.example.back.profile.exception.ConflictException;
import org.example.back.profile.exception.InternalServerErrorException;
import org.example.back.profile.exception.ProfileNameAlreadyExistException;
import org.example.back.profile.exception.ProfileNotFoundException;
import org.example.back.profile.exception.UnauthorizedException;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.ProfileService;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.example.back.profileimage.entity.ProfileImage;
import org.example.back.profileimage.repository.ProfileImageRepository;
import org.example.back.swipe.entity.Swipe;
import org.example.back.swipe.repository.SwipeRepository;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.InvalidTokenException;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.UserRepository;
import org.example.back.user.service.UserService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
	private final SwipeRepository swipeRepository;

	@Value("${app.profile.default-image-url}")
	private String defaultProfileImageUrl;

	@PostMapping
	@Override
	public ResponseEntity<ProfileDto> createProfile(@Valid @RequestBody final ProfileCreateRequest request,
		HttpServletRequest httpServletRequest) {

		// 1. 토큰 검증
		String token = resolveToken(httpServletRequest);
		if (token == null) {
			throw new UnauthorizedException("토큰이 없습니다");
		}

		// 2. 토큰에서 userId 추출
		String userIdToken;
		try {
			userIdToken = jwtTokenProvider.extractSubject(token);
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException("만료된 토큰입니다");
		} catch (JwtException e) {
			throw new UnauthorizedException("유효하지 않은 토큰입니다");
		}

		// 3. userId 변환
		Long userId;
		try {
			userId = Long.valueOf(userIdToken);
		} catch (NumberFormatException e) {
			throw new BadRequestException("유효하지 않은 사용자 ID 형식입니다");
		}

		// 4. 중복 프로필 체크
		if (profileRepository.findByUserId(userId).isPresent()) {
			throw new ConflictException("이미 프로필이 존재합니다");
		}

		if (profileRepository.findProfileByProfileName(request.profileName()).isPresent()){
			throw new ProfileNameAlreadyExistException();
		}

		// 5. 프로필 생성
		try {
			ProfileDto profileDto = profileService.createProfile(request, userId);
			return ResponseEntity.status(HttpStatus.CREATED).body(profileDto);
		} catch (Exception e) {
			throw new InternalServerErrorException("프로필 생성 중 오류가 발생했습니다: " + e.getMessage());
		}
	}


	@PatchMapping("/update")
	@Override
	public ResponseEntity<ProfileDto> updateProfile(@Valid @RequestBody final ProfileUpdateRequest request,
		HttpServletRequest httpServletRequest) {

		// 1. 토큰 검증
		String token = resolveToken(httpServletRequest);
		if (token == null) {
			throw new UnauthorizedException("토큰이 없습니다");
		}

		// 2. 토큰에서 userId 추출
		String userIdToken;
		try {
			userIdToken = jwtTokenProvider.extractSubject(token);
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException("만료된 토큰입니다");
		} catch (JwtException e) {
			throw new UnauthorizedException("유효하지 않은 토큰입니다");
		}

		// 3. userId 변환
		Long userId;
		try {
			userId = Long.valueOf(userIdToken);
		} catch (NumberFormatException e) {
			throw new BadRequestException("유효하지 않은 사용자 ID 형식입니다");
		}


		if (profileRepository.findProfileByProfileName(request.profileName()).isPresent()){
			throw new ProfileNameAlreadyExistException();
		}

		// 5. 프로필 생성
		try {
			ProfileDto profileDto = profileService.updateProfile(request, userId);
			return ResponseEntity.status(HttpStatus.CREATED).body(profileDto);
		} catch (Exception e) {
			throw new InternalServerErrorException("프로필 생성 중 오류가 발생했습니다: " + e.getMessage());
		}
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

		// Optional<ProfileImage> image= profileImageRepository.findFirstByProfile_ProfileId(profileId);

		ProfileImage profileImage = profileImageRepository.findFirstByProfile_ProfileId(profileId)
			.orElseGet(() -> {
				// 기본 이미지로 새 ProfileImage 생성
				ProfileImage defaultImage = ProfileImage.of(
					profile,
					defaultProfileImageUrl,
					0  // 기본 이미지 크기
				);
				return profileImageRepository.save(defaultImage);
			});

		profileService.updateProfileLocation(profileId, locationRequest.longitude(),
			locationRequest.latitude());

		return ResponseEntity.ok(new ProfileDto(profileImage.getImageUrl(),profile.getProfileName(),profile.getAge()));
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


	@GetMapping("/{profileId}")
	@Override
	public ResponseEntity<ProfileInfoDto> getProfileInfo(
		HttpServletRequest request,
		@PathVariable("profileId") Long profileId
	) {
		// 1. 토큰 검증
		String token = resolveToken(request);
		if (token == null) {
			throw new UnauthorizedException("토큰이 없습니다");
		}

		// 2. 토큰에서 userId 추출
		String userIdToken;
		try {
			userIdToken = jwtTokenProvider.extractSubject(token);
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException("만료된 토큰입니다");
		} catch (JwtException e) {
			throw new UnauthorizedException("유효하지 않은 토큰입니다");
		}

		ProfileInfoDto profileDto = profileService.getProfileInfo(profileId);
		System.out.println(profileDto);
		return ResponseEntity.ok(profileDto);
	}




	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}


	@GetMapping("/getProfile")
	public ResponseEntity<ProfileResponseDto> getProfile(HttpServletRequest request) {

		String token = resolveToken(request);
		String userIdToken = jwtTokenProvider.extractSubject(token);
		Long userId = Long.valueOf(userIdToken);
		Long profileId = userService.getProfileIdByUserId(userId);

		Profile userProfile = profileService.findProfileByProfileId(profileId);

		// profileId로 프로필 이미지들을 조회하여 다수의 결과 처리
		List<ProfileImage> profileImages = profileImageRepository.findAllByProfile_ProfileId(profileId);

		// 이미지 URL 리스트 추출
		List<String> imageUrls = profileImages.stream()
			.map(ProfileImage::getImageUrl)
			.toList();

		// 이미지 URL 리스트를 ProfileResponseDto에 전달
		ProfileResponseDto responseDTO = ProfileResponseDto.from(userProfile, imageUrls);

		return ResponseEntity.ok(responseDTO);
	}


	private boolean checkProfileMatch(Long profileId, Long getProfileId) {
		List<Swipe> swipes = swipeRepository.findByFromProfileIdOrToProfileId(profileId, getProfileId);

		return swipes.stream()
			.anyMatch(swipe ->
				(swipe.getFromProfileId().equals(profileId) && swipe.getToProfileId().equals(getProfileId)) ||
					(swipe.getFromProfileId().equals(getProfileId) && swipe.getToProfileId().equals(profileId))
			);
	}
}
