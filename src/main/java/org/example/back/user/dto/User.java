package org.example.back.user.dto;

import org.example.back.user.entity.UserEntity;

public record User(
	String email
) {

	public static User from(UserEntity userEntity) {
		return new User(
			userEntity.getEmail()
		);
	}
}
