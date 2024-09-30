package org.example.back.user.service;

import org.example.back.config.provider.AuthTokens;
import org.example.back.config.provider.AuthTokensGenerator;
import org.example.back.config.provider.EmailProvider;
import org.example.back.config.provider.JwtTokenProvider;
import org.example.back.user.dto.User;
import org.example.back.user.dto.request.CheckCertificationRequestDto;
import org.example.back.user.dto.request.EmailCertificationRequestDto;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.request.SignUpRequestDto;
import org.example.back.user.dto.response.CheckCertificationResponseDto;
import org.example.back.user.dto.response.EmailCertificationResponseDto;
import org.example.back.user.dto.response.SignInResponseDto;
import org.example.back.user.entity.CertificationEntity;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.UserAlreadyExistsException;
import org.example.back.user.exception.UserEmailNotAllowedException;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.CertificationRepository;
import org.example.back.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final EmailProvider emailProvider;
	private final CertificationRepository certificationRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthTokensGenerator authTokensGenerator;

	public EmailCertificationResponseDto emailCertification(EmailCertificationRequestDto requestBody) {

		String username = requestBody.username();
		String email = requestBody.email();

		userRepository.findByUsername(username).ifPresent(
			user -> {
				throw new UserAlreadyExistsException(username);
			}
		);

		String certificationNumber = CertificationNumber.getCertificationNumber();
		boolean isSucceed = emailProvider.sendCertification(email, certificationNumber);

		if (!isSucceed) {
			return new EmailCertificationResponseDto("실패", "메일 전송에 실패했습니다");

		}
		CertificationEntity certificationEntity = new CertificationEntity(email, username, certificationNumber);
		certificationRepository.save(certificationEntity);

		return new EmailCertificationResponseDto("성공", "이메일 요청이 성공하였습니다");

	}

	public CheckCertificationResponseDto checkCertificationNumber(CheckCertificationRequestDto dto) {

		String email = dto.email();
		String certificationNumber = dto.certificationNumber();

		CertificationEntity certificationEntity = certificationRepository.findByEmail(email);

		if (certificationEntity == null) {
			return new CheckCertificationResponseDto("실패", "");
		}
		boolean isMatched =
			certificationEntity.getEmail().equals(email) &&
				certificationEntity.getCertificationNumber().equals(certificationNumber);

		if (!isMatched) {
			return new CheckCertificationResponseDto("실패", "인증에 실패했습니다");
		}

		return new CheckCertificationResponseDto("성공", "인증에 성공했습니다");
	}

	@Transactional
	public User signUp(SignUpRequestDto dto) {

		String username = dto.username();
		userRepository.findByUsername(username).ifPresent(
			user ->
			{
				throw new UserAlreadyExistsException(username);
			}
		);
		String email = dto.email();
		CertificationEntity certificationEntity = certificationRepository.findByEmail(email);

		boolean isMatched = certificationEntity.getEmail().equals(email);

		if (!isMatched) {
			throw new UserEmailNotAllowedException(email);
		}

		String password = dto.password();
		//추후에 인코딩
		String encodedPassword = password;

		UserEntity userEntity = UserEntity.ofBase("Base", email, encodedPassword, username, "USER");

		userRepository.save(userEntity);
		certificationRepository.deleteByUsername(username);

		return User.from(userEntity);

	}

	// public SignInResponseDto signIn(SignInRequestDto dto) {
	//
	// 	AuthTokens token = null;
	// 	String username = dto.username();
	//
	// 	UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(
	// 		() -> new UserNotFoundException(username)
	// 	);
	//
	// 	String password = dto.password();
	// 	String encodedPassword = userEntity.getPassword();
	//
	// 	token = authTokensGenerator.generate(userEntity.getUserId());
	//
	// 	return new SignInResponseDto(token, 3600);
	// }

	//jwtProvider에서 유저 이름을 가지고 jwt를 생성
	public SignInResponseDto signIn(SignInRequestDto dto) {

		AuthTokens authTokens=null;
		String token = null;
		String username = dto.username();
		UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(
			() -> new UserNotFoundException(username)
		);

		String password = dto.password();
		String encodedPassword = userEntity.getPassword();

		// 비밀번호 확인 로직 추가 필요
		// 예: if (!passwordEncoder.matches(password, encodedPassword)) { ... }

		// AuthTokens 생성, 유저 ID와 리프레시 토큰,
		AuthTokens tokens = authTokensGenerator.generate(userEntity.getUserId());

		return new SignInResponseDto(authTokens, 3600L);
	}


	private static class CertificationNumber {

		public static String getCertificationNumber() {
			String certificationNumber = "";

			for (int count = 0; count < 4; count++) {
				certificationNumber += (int)(Math.random() * 10);
			}
			return certificationNumber;
		}
	}

}
