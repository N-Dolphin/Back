package org.example.back.profile.domain;

import org.example.back.profile.domain.type.Gender;
import org.example.back.user.entity.UserEntity;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long profileId;

	@Column
	private String nickname;

	@Column
	private String selfIntroduce;

	@Column
	private Integer age;

	@Enumerated(EnumType.STRING)
	@Column
	private Gender gender;

	//유저와 직접 매핑하기 보다, 성능 최적화를 위해 id를 매핑
	@Column(name = "user_id", nullable = false) // 외래 키로 사용할 컬럼
	private Long userId;

	@Embedded
	private ProfileLocation location;


}
