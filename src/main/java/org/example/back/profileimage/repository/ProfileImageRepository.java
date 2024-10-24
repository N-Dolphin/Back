package org.example.back.profileimage.repository;

import java.util.List;
import java.util.Optional;

import org.example.back.profileimage.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
	// 각 프로필에 대한 첫 번째 이미지를 가져오는 쿼리
	Optional<ProfileImage> findFirstByProfile_ProfileId(Long profileId);

	Optional<ProfileImage> findByProfile_ProfileId(Long profileId);

	// 해당 프로필의 모든 이미지를 가져오는 쿼리
	List<ProfileImage> findAllByProfile_ProfileId(Long profileId);
}
