package org.example.back.profile.domain;

import org.locationtech.jts.geom.Point;
import java.util.List;

public record ProfileResponseDto(
	Long profileId,
	String profileName,
	String selfIntroduce,
	Integer age,
	String gender,
	LocationDto location,
	List<String> profileImages // 이미지 URL 리스트 추가
) {
	public static ProfileResponseDto from(Profile profile, List<String> imageUrls) {
		return new ProfileResponseDto(
			profile.getProfileId(),
			profile.getProfileName(),
			profile.getSelfIntroduce(),
			profile.getAge(),
			profile.getGender().toString(),
			profile.getLocation() != null ? LocationDto.from(profile.getLocation().getLocation()) : null,
			imageUrls // 이미지 URL 리스트를 받아서 설정
		);
	}
}
