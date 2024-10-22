package org.example.back.profile.domain;

import org.locationtech.jts.geom.Point;

public record LocationDto(
	double longitude, double latitude
) {
	public static LocationDto from(Point point) {
		return  new LocationDto(point.getX(), point.getY());
	}
}