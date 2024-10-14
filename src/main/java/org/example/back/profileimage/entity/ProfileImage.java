package org.example.back.profileimage;

import org.example.back.profile.domain.Profile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class ProfileImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	// 프로필 아이디
	// 프로필과의 연관관계 (다대일)
	@ManyToOne
	@JoinColumn(name = "profile_id")
	private Profile profile;

	String imageUrl;

	Integer imageSize;

	String imageOriginalName;

	String imageStoredName;

}
