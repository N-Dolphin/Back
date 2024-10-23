package org.example.back.user.service;

import java.util.Optional;

import org.example.back.config.provider.AuthTokens;
import org.example.back.config.provider.AuthTokensGenerator;
import org.example.back.profile.domain.Profile;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profileimage.repository.ProfileImageRepository;
import org.example.back.user.dto.response.SignInResponseDto;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.oauth.OAuthInfoResponse;
import org.example.back.user.oauth.OAuthLoginParams;
import org.example.back.user.oauth.OAuthProvider;
import org.example.back.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {
	private final UserRepository userRepository;
	private final AuthTokensGenerator authTokensGenerator;
	private final RequestOAuthInfoService requestOAuthInfoService;
	private final ProfileRepository profileRepository;
	private final ProfileImageRepository profileImageRepository;

	public SignInResponseDto login(OAuthLoginParams params) {
		OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
		UserEntity userEntity = findOrCreateMember(oAuthInfoResponse);

		Optional<Profile> profile = profileRepository.findByUserId(userEntity.getUserId());
		boolean hasProfile = profile.isPresent();
		boolean hasProfileLocation = profile.map(Profile::getLocation).isPresent();
		boolean hasProfileImage = profile.map(Profile::getProfileId)
			.flatMap(profileImageRepository::findFirstByProfile_ProfileId) // 여기 수정
			.isPresent();
		
		return new SignInResponseDto(authTokensGenerator.generate(userEntity.getUserId()),3600L, hasProfile,hasProfileImage,hasProfileLocation);
	}

	private UserEntity findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
		return userRepository.findByEmail(oAuthInfoResponse.getEmail()).orElse(
			newMember(oAuthInfoResponse)
		);
	}

	private UserEntity newMember(OAuthInfoResponse oAuthInfoResponse) {
		UserEntity userEntity = UserEntity.ofOauth("Oauth", oAuthInfoResponse.getEmail(),null,"USER",
			OAuthProvider.KAKAO);
		return userRepository.save(userEntity);
	}
}
