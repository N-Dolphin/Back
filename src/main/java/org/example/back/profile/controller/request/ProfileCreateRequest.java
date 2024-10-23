package org.example.back.profile.controller.request;

import java.time.LocalDate;
import java.time.Period;

import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.type.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(name = "프로필 생성 Request")
public record ProfileCreateRequest(

	@Schema(description = "유저명", example = "피카츄")
	@NotBlank
	String profileName,

	@Schema(description = "자기소개", example = "안녕하세요")
	@NotBlank
	String selfIntroduction,

	@Schema(description = "생년월일 (yyyy-MM-dd)", example = "1998-01-01")
	@NotBlank
	@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-MM-dd이어야 합니다.")
	String dateOfBirth,

	@Schema(description = "성별", example = "MALE")
	@NotNull
	Gender gender
) {
	public Profile toProfile() {
		LocalDate birthDate = LocalDate.parse(this.dateOfBirth); // 생년월일을 LocalDate로 변환
		return Profile.builder()
			.profileName(this.profileName)
			.selfIntroduce(this.selfIntroduction)
			.dateOfBirth(birthDate) // 생년월일을 설정
			.gender(this.gender)
			.build();
	}
}
