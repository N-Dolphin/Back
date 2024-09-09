package org.example.back.user.oauth;
import org.springframework.util.MultiValueMap;

public interface OAuthLoginParams {
	OAuthProvider oAuthProvider();
	MultiValueMap<String, String> makeBody();
}