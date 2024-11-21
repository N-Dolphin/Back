package org.example.back.profile.domain;

import java.time.LocalDate;
import java.time.Period;

import org.example.back.profile.domain.type.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "profile")  // 테이블 이름을 소문자로 지정
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long profileId;

	@Column
	private String profileName;

	@Column
	private String selfIntroduce;

	@Enumerated(EnumType.STRING)
	@Column
	private Gender gender;

	//유저와 직접 매핑하기 보다, 성능 최적화를 위해 id를 매핑
	@Column(name = "user_id", nullable = false) // 외래 키로 사용할 컬럼
	private Long userId;

	@Embedded
	private ProfileLocation location;

	// Optional: 생년월일을 저장하는 필드 추가
	@Column(name = "date_of_birth") // 추가된 생년월일 컬럼
	private LocalDate dateOfBirth;

	// 나이 계산 메서드
	public Integer getAge() {
		return Period.between(dateOfBirth, LocalDate.now()).getYears();
	}

}
