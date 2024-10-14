package org.example.back.profileimage.entity;

import org.example.back.profile.domain.Profile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "profile_image") // 테이블 이름을 명시적으로 지정
public class ProfileImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "profile_id")
	private Profile profile;

	private String imageUrl;
	private Integer imageSize;


	public static ProfileImage of(Profile profile, String imageUrl, Integer imageSize) {
		ProfileImage profileImage = new ProfileImage();
		profileImage.setProfile(profile);
		profileImage.setImageUrl(imageUrl);
		profileImage.setImageSize(imageSize);
		return profileImage;
	}
}
