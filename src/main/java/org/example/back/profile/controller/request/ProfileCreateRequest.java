package org.example.back.profile.controller.request;

import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.type.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileCreateRequest(
	@NotBlank
	String nickname,
	@NotBlank
	String selfIntroduction,
	@NotNull
	Integer age,
	@NotNull
	Gender gender
) {
	public Profile toProfile() {
		return Profile.builder()
			.nickname(this.nickname)
			.selfIntroduce(this.selfIntroduction)
			.age(this.age)
			.gender(this.gender)
			.build();
	}
}
