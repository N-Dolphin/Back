package org.example.back.interest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Interest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	// 프로필 id

	// TODO: Interest Enum -> enum타입으로 받아올지 프론트에서 입력값을 받을지?
}
