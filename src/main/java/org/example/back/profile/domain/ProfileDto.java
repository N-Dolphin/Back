package org.example.back.profile.domain;

import java.time.LocalDate;
import java.time.Period;

public record ProfileDto(
	String imageUrl,
	String profileName,
	int age
) {
	public static ProfileDto of(Profile profile, String url) {
		return new ProfileDto(
			url,
			profile.getProfileName(),
			calculateAge(profile.getDateOfBirth()) // 생년월일로 나이 계산
		);
	}
	private static int calculateAge(LocalDate dateOfBirth) {
		if (dateOfBirth == null) {
			return 0; // 생년월일이 없을 경우 기본값 처리
		}
		return Period.between(dateOfBirth, LocalDate.now()).getYears();
	}
}
