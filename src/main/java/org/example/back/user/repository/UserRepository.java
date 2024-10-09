package org.example.back.user.repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

import org.example.back.profile.domain.Profile;
import org.example.back.user.entity.UserEntity;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByUsername(String username);

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByUserId(Long userId);
}
