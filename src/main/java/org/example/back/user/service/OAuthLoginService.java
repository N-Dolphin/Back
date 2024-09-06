package org.example.back.user.service;

import org.example.back.config.provider.AuthTokens;
import org.example.back.config.provider.AuthTokensGenerator;
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

	public AuthTokens login(OAuthLoginParams params) {
		OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
		UserEntity userEntity = findOrCreateMember(oAuthInfoResponse);
		return authTokensGenerator.generate(userEntity.getUserId());
	}

	private UserEntity findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
		return userRepository.findByEmail(oAuthInfoResponse.getEmail()).orElse(
			newMember(oAuthInfoResponse)
		);
	}

	private UserEntity newMember(OAuthInfoResponse oAuthInfoResponse) {
		UserEntity userEntity = UserEntity.ofOauth("Oauth", oAuthInfoResponse.getEmail(),null,oAuthInfoResponse.getNickname(),"USER",
			OAuthProvider.KAKAO);
		return userRepository.save(userEntity);
	}
}
