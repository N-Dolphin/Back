package org.example.back.chat.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long fromProfileId;
	private Long toProfileId;

	private LocalDateTime createdAt = LocalDateTime.now();

	private LocalDateTime lastActivity = LocalDateTime.now();

	@ElementCollection
	private Set<Long> activeParticipants = new HashSet<>();

	@Column
	private boolean isActive = true;

	public void addParticipant(Long profileId) {
		activeParticipants.add(profileId);
	}

	public void removeParticipant(Long profileId) {
		activeParticipants.remove(profileId);
	}

	public void updateLastActivity() {
		this.lastActivity = LocalDateTime.now();
	}
}
