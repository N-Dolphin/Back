package org.example.back.user.oauth;

public interface OAuthInfoResponse {
	String getEmail();
	String getNickname();
	OAuthProvider getOAuthProvider();
}
