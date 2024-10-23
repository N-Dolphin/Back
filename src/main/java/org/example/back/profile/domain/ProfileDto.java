package org.example.back.profile.domain;

import org.example.back.profileimage.entity.ProfileImage;

public record ProfileDto(
	String imageUrl,
	String profileName,
	int age
) {
	public static ProfileDto of(Profile profile,String url)
	{
		return new ProfileDto(
			url,
			profile.getProfileName(),
			profile.getAge()
		);
	}
}
