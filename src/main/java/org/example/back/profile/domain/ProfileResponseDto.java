package org.example.back.profile.domain;

import org.locationtech.jts.geom.Point;
import java.util.List;

public record ProfileResponseDto(
	Long profileId,
	String profileName,
	String selfIntroduction,
	Integer age,
	String gender,
	LocationDto location,
	List<String> profileImages // 이미지 URL 리스트 추가
) {
	public static ProfileResponseDto from(Profile profile, List<String> imageUrls) {
		if (profile == null) {
			// profile이 null일 경우 기본값 반환
			return new ProfileResponseDto(
				null, // profileId
				"",   // profileName
				"",   // selfIntroduction
				null, // age
				"",   // gender
				null, // location
				imageUrls != null ? imageUrls : List.of() // 이미지 URL 리스트가 null인 경우 빈 리스트 반환
			);
		}

		// profile이 존재하는 경우의 응답
		return new ProfileResponseDto(
			profile.getProfileId(),
			profile.getProfileName(),
			profile.getSelfIntroduction(),
			profile.getAge(),
			profile.getGender() != null ? profile.getGender().toString() : "",
			profile.getLocation() != null ? LocationDto.from(profile.getLocation().getLocation()) : null,
			imageUrls != null ? imageUrls : List.of() // 이미지 URL 리스트가 null인 경우 빈 리스트 반환
		);
	}

}
