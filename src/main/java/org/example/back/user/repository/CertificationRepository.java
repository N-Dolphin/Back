package org.example.back.user.repository;

import org.example.back.user.entity.CertificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<CertificationEntity, Long> {

	CertificationEntity findByEmail(String email);

	void deleteByEmail(String email);

}
