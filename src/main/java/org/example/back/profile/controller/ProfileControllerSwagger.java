package org.example.back.profile.controller;

import org.apache.coyote.Response;
import org.example.back.profile.controller.request.ProfileCreateRequest;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Profile API")
public interface ProfileControllerSwagger {

	@Operation(
		summary = "프로필 생성",
		requestBody = @RequestBody(content =
		@Content(schema = @Schema(implementation = ProfileCreateRequest.class))),
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "프로필 생성 성공"
			)
		}
	)
	ResponseEntity<Void> createProfile(final ProfileCreateRequest request);
}
