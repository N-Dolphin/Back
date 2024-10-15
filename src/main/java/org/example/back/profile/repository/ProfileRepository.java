package org.example.back.profile.repository;

import java.util.List;
import java.util.Optional;

import org.example.back.profile.domain.Profile;
import org.example.back.profile.domain.ProfileDistance;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
	Optional<Profile> findByUserId(Long userId);

	Optional<Profile> findByProfileId(Long profileId);

	@Query(value = "SELECT * FROM profile p " +
		"WHERE ST_DWithin(" +
		"geography(p.location), " + // 프로필의 위치
		"geography(ST_SetSRID(ST_Point(:longitude, :latitude), 4326)), " + // 청와대의 위치
		"300000)", nativeQuery = true)
	List<Profile> findProfilesWithinDistance(@Param("longitude") double longitude, @Param("latitude") double latitude);


	@Query("SELECT p FROM Profile p")
	List<Profile> findProfilesWithoutImages();

	@Query("SELECT p FROM Profile p LEFT JOIN ProfileImage pi ON pi.profile.profileId = p.profileId WHERE p.profileId = :profileId")
	Optional<Profile> findProfileWithImages(@Param("profileId") Long profileId);


	@Query("SELECT p FROM Profile p LEFT JOIN ProfileImage pi ON pi.profile.profileId = p.profileId WHERE pi.profile.profileId IS NOT NULL")
	List<Profile> findProfilesWithImages();



	@Query(value = "SELECT p.* FROM profile p " +
		"WHERE ST_DWithin(geography(p.location), geography(:currentLocation), :radius) " +
		"AND p.profileid != :profileId " +  // profile_id -> profileid로 수정
		"ORDER BY ST_Distance(geography(p.location), geography(:currentLocation)) ASC",
		nativeQuery = true)
	List<Profile> findProfilesSortedByDistance(@Param("currentLocation") Point currentLocation,
		@Param("profileId") Long profileId,
		@Param("radius") double radius);

	Profile findProfileByNickname(String toUsername);
}
