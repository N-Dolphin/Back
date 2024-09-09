package org.example.back.user.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.back.user.oauth.OAuthApiClient;
import org.example.back.user.oauth.OAuthInfoResponse;
import org.example.back.user.oauth.OAuthLoginParams;
import org.example.back.user.oauth.OAuthProvider;
import org.springframework.stereotype.Component;

@Component
public class RequestOAuthInfoService {
	private final Map<OAuthProvider, OAuthApiClient> clients;

	public RequestOAuthInfoService(List<OAuthApiClient> clients) {
		this.clients = clients.stream().collect(
			Collectors.toUnmodifiableMap(OAuthApiClient::oAuthProvider, Function.identity())
		);
	}

	public OAuthInfoResponse request(OAuthLoginParams params) {
		OAuthApiClient client = clients.get(params.oAuthProvider());
		String accessToken = client.requestAccessToken(params);
		return client.requestOauthInfo(accessToken);
	}
}
