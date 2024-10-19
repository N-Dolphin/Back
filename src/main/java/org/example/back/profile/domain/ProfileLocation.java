package org.example.back.profile.domain;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@NoArgsConstructor
@ToString
public class ProfileLocation {
	
	private Point location;

	public ProfileLocation(Double longitude, Double latitude) {
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
		location = geometryFactory.createPoint(new Coordinate(longitude, latitude));


	}
}