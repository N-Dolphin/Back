package org.example.back.profile.domain;

import org.example.back.profileimage.entity.ProfileImage;

public record ProfileDto(
	String imageUrl,
	String profileName,
	int age
) {
}
