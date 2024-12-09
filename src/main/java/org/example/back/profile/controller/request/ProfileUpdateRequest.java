package org.example.back.profile.controller.request;

import java.time.LocalDate;

import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.type.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(name = "프로필 생성 Request")
public record ProfileUpdateRequest(

	@Schema(description = "유저명", example = "피카츄")
	@NotBlank
	String profileName,

	@Schema(description = "자기소개", example = "안녕하세요")
	@NotBlank
	String selfIntroduction


) {
	public Profile toUpdateProfile(Profile profile) {
		return Profile.builder()
			.profileName(this.profileName)
			.selfIntroduction(this.selfIntroduction)
			.build();
	}
}
