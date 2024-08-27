package org.example.back.profile.controller;

import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.service.ProfileService;
import org.example.back.profile.service.response.ProfileCreateResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class ProfileController implements ProfileControllerSwagger {

	private final ProfileService profileService;

	@PostMapping
	@Override
	public ResponseEntity<ProfileCreateResponse> createProfile(@Valid @RequestBody final ProfileCreateRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(request));
	}

}
