package org.example.back.profile.controller.request;

import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.type.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "프로필 생성 Request")
public record ProfileCreateRequest(

	@Schema(description = "유저명", example = "피카츄")
	@NotBlank
	String profileName,
	@Schema(description = "자기소개", example = "안녕하세요")
	@NotBlank
	String selfIntroduction,
	@Schema(description = "나이", example = "23살")
	@NotNull
	Integer age,
	@Schema(description = "성별", example = "MALE")
	@NotNull
	Gender gender
) {
	public Profile toProfile() {
		return Profile.builder()
			.profileName(this.profileName)
			.selfIntroduce(this.selfIntroduction)
			.age(this.age)
			.gender(this.gender)
			.build();
	}
}
