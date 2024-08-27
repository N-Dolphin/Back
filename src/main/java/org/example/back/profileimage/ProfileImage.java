package org.example.back.profileimage;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ProfileImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	// 프로필 아이디

	String imageUrl;

	Integer imageSize;

	String imageOriginalName;

	String imageStoredName;

}
