package org.example.back.chat.config;

import java.time.LocalDateTime;

import lombok.Value;

//이후의 프로필(유저)의 상태를 표현하기 위한 클래스
@Value
public class UserPresence {
	Long profileId;
	PresenceStatus status;
	LocalDateTime lastSeen;

	public enum PresenceStatus {
		ONLINE, OFFLINE, AWAY
	}
}