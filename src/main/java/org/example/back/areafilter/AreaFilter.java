package org.example.back.areafilter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AreaFilter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	// profile id;

	String address;

	Double longitude;

	Double latitude;

	// Postgist point 좌표 4326

}
