package org.example.back.profileimage.repository;

import org.example.back.profileimage.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
	// 각 프로필에 대한 첫 번째 이미지를 가져오는 쿼리
	ProfileImage findFirstByProfile_ProfileId(Long profileId);
}

