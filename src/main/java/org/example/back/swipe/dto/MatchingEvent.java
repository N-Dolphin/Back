package org.example.back.swipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingEvent implements Serializable {
	private Long fromProfileId;
	private Long toProfileId;
}
