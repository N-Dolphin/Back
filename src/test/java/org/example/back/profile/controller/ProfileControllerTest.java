package org.example.back.profile.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.domain.type.Gender;
import org.example.back.profile.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProfileService profileService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@DisplayName("프로필을 생성한다")
	@Test
	void createProfile() throws Exception {

		ProfileCreateRequest request = new ProfileCreateRequest(
			"피카츄", "안녕하세요 피카츄입니다.", 23, Gender.MALE
		);

		String requestBody = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/v1/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andDo(print())
			.andExpect(status().isCreated());
	}
}