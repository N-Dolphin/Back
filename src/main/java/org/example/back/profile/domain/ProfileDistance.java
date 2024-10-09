package org.example.back.profile.domain;

public class ProfileDistance {
	private Long id;       // 프로필 ID
	private double distance; // 거리 (미터 단위)

	public ProfileDistance(Long id, double distance) {
		this.id = id;
		this.distance = distance;
	}

	public Long getId() {
		return id;
	}

	public double getDistance() {
		return distance;
	}
	@Override
	public String toString() {
		return "ProfileDistance{id=" + id + ", distance=" + distance + "}";
	}
}
