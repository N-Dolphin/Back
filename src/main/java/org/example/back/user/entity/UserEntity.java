package org.example.back.user.entity;

import org.example.back.profile.domain.Profile;
import org.example.back.user.oauth.OAuthInfoResponse;
import org.example.back.user.oauth.OAuthProvider;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "\"user\"")

@Getter
@Setter
@ToString
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column
	private String email;

	@Column
	private String username;

	@Column
	private String loginType;

	@Column
	private String password;

	@Column
	private String role;

	@Column
	private OAuthProvider oAuthProvider;

	@Column
	@JoinColumn(name = "profile_id")
	private Long profileId;

	public static UserEntity ofBase(String loginType, String email, String password, String username, String role) {
		var userEntity = new UserEntity();
		userEntity.setLoginType(loginType);
		userEntity.setEmail(email);
		userEntity.setPassword(password);
		userEntity.setUsername(username);
		userEntity.setRole(role);
		return userEntity;
	}


	public static UserEntity ofOauth(String loginType, String email, String password, String username, String role, OAuthProvider oAuthProvider) {
		var userEntity = new UserEntity();
		userEntity.setLoginType(loginType);
		userEntity.setEmail(email);
		userEntity.setPassword(password);
		userEntity.setUsername(username);
		userEntity.setRole(role);
		userEntity.setOAuthProvider(oAuthProvider);
		return userEntity;
	}



}

