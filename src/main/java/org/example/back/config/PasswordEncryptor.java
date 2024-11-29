package org.example.back.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryptor {

	private static final String ALGORITHM = "SHA-256";

	@Value("${jwt.secret-key}")
	private String SALT;

	public String encrypt(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
			md.update((password + SALT).getBytes());
			byte[] byteData = md.digest();

			StringBuilder sb = new StringBuilder();
			for (byte byteDatum : byteData) {
				sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("비밀번호 암호화 실패", e);
		}
	}

	public boolean matches(String rawPassword, String encodedPassword) {
		String hashedPassword = encrypt(rawPassword);
		return hashedPassword.equals(encodedPassword);
	}
}