package org.example.back.profile.service.response;

import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.type.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "프로필 생성 Response")
public record ProfileCreateResponse(

	@Schema(description = "프로필 ID", example = "1")
	Long id,

	@Schema(description = "유저명", example = "피카츄")
	String nickname,

	@Schema(description = "유저 나이", example = "30")
	Integer age,

	@Schema(description = "자기소개", example = "안녕하세요")
	String selfIntroduction,

	@Schema(description = "성별", example = "남자")
	Gender gender
) {

	public static ProfileCreateResponse of(final Profile newProfile) {
		return new ProfileCreateResponse(
			newProfile.getProfileId(),
			newProfile.getProfileName(),
			newProfile.getAge(),
			newProfile.getSelfIntroduce(),
			newProfile.getGender()
		);
	}
}
