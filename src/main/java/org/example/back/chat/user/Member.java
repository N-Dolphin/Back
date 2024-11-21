package org.example.back.chat.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Member {

	private Long profileId;  // username 대신 profileId로 변경

	public Member(Long profileId) {
		this.profileId= profileId;
	}
}