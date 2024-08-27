package org.example.back.profile.controller;

import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.example.back.profile.service.ProfileService;
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
public class ProfileController implements ProfileControllerSwagger{

	private final ProfileService profileService;

	@PostMapping
	@Override
	public ResponseEntity<Void> createProfile(@Valid @RequestBody final ProfileCreateRequest request) {
		profileService.createProfile(request);
		return ResponseEntity.ok().build();
	}

}
