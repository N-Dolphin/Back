package org.example.back.user.oauth;

public interface OAuthApiClient {
	OAuthProvider oAuthProvider();
	String requestAccessToken(OAuthLoginParams params);
	OAuthInfoResponse requestOauthInfo(String accessToken);
}