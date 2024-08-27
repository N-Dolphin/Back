package org.example.back.matching;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Matching {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	//유저 1

	//유저 2
}
