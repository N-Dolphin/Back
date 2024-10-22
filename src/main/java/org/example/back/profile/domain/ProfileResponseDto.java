package org.example.back.profile.domain;

import org.locationtech.jts.geom.Point;

public record ProfileResponseDto(
	String profileName,
	String selfIntroduce,
	Integer age,
	String gender,
	LocationDto location
) {
	public static ProfileResponseDto from (Profile profile) {
		return new ProfileResponseDto(
			profile.getProfileName(),
			profile.getSelfIntroduce(),
			profile.getAge(),
			profile.getGender().toString(),
			profile.getLocation() != null ? LocationDto.from(profile.getLocation().getLocation()) : null
		);
	}
}


