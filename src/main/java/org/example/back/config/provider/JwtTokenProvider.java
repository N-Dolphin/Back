package org.example.back.config.provider;

import java.security.Key;
import java.util.Date;

import org.example.back.user.exception.InvalidTokenException; // Import the InvalidTokenException
import org.example.back.user.exception.TokenExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final Key key;

	public JwtTokenProvider(@Value("${jwt.secret-key2}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generate(String subject, Date expiredAt) {
		return Jwts.builder()
			.setSubject(subject)
			.setExpiration(expiredAt)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();
	}

	public String extractSubject(String accessToken) {
		Claims claims = parseClaims(accessToken);
		return claims.getSubject();
	}

	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parser()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(accessToken)
				.getBody();
		} catch (ExpiredJwtException e) {
			throw new TokenExpiredException();  // 새로운 예외 타입 사용
		} catch (JwtException e) {
			throw new InvalidTokenException("유효하지 않은 토큰");
		}
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
		} catch (TokenExpiredException e) {
			throw e;  // TokenExpiredException은 그대로 전파
		} catch (JwtException e) {
			throw new InvalidTokenException("유효하지 않은 토큰");
		}
		return true;
	}
}
