package org.example.back.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.back.config.provider.EmailProvider;
import org.example.back.config.provider.JwtProvider;
import org.example.back.user.dto.User;
import org.example.back.user.dto.request.CheckCertificationRequestDto;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.request.SignUpRequestDto;
import org.example.back.user.dto.response.CheckCertificationResponseDto;
import org.example.back.user.dto.request.EmailCertificationRequestDto;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailProvider emailProvider;
    private final CertificationRepository certificationRepository;
    private final JwtProvider jwtProvider;

    public EmailCertificationResponseDto emailCertification(EmailCertificationRequestDto requestBody) {


        String username= requestBody.username();
        String email= requestBody.email();

        userRepository.findByUsername(username).ifPresent(
                user -> {
                    throw new UserAlreadyExistsException(username);
                }
        );

        String certificationNumber= CertificationNumber.getCertificationNumber();
        boolean isSucceed=emailProvider.sendCertification(email,certificationNumber);

        if (!isSucceed) {
            return new EmailCertificationResponseDto("실패", "메일 전송에 실패했습니다");

        }
        CertificationEntity certificationEntity= new CertificationEntity(email,username,certificationNumber);
        certificationRepository.save(certificationEntity);

        return new EmailCertificationResponseDto("성공", "이메일 요청이 성공하였습니다");


    }


    public CheckCertificationResponseDto checkCertificationNumber(CheckCertificationRequestDto dto) {

        String email=dto.email();
        String certificationNumber= dto.certificationNumber();

        CertificationEntity certificationEntity= certificationRepository.findByEmail(email);

        if (certificationEntity==null){
            return new CheckCertificationResponseDto("실패","");
        }
        boolean isMatched=
                certificationEntity.getEmail().equals(email)&&
                certificationEntity.getCertificationNumber().equals(certificationNumber);

        if (!isMatched){
            return new CheckCertificationResponseDto("실패","인증에 실패했습니다");
        }

        return new CheckCertificationResponseDto("성공", "인증에 성공했습니다");
    }

    @Transactional
    public User signUp(SignUpRequestDto dto) {


        String username=dto.username();
        userRepository.findByUsername(username).ifPresent(
                user ->
                {
                    throw new UserAlreadyExistsException(username);
                }
        );
        String email= dto.email();
        CertificationEntity certificationEntity= certificationRepository.findByEmail(email);

        boolean isMatched= certificationEntity.getEmail().equals(email);

        if (!isMatched){
           throw new UserEmailNotAllowedException(email);
        }

        String password= dto.password();
        //추후에 인코딩
        String encodedPassword= password;


        UserEntity userEntity= UserEntity.of("Base",email,encodedPassword,username,"USER");
        userRepository.save(userEntity);

        certificationRepository.deleteByUsername(username);

        return User.from(userEntity);

    }

    public SignInResponseDto signIn(SignInRequestDto dto) {

        String token= null;
        String username= dto.username();

        UserEntity userEntity= userRepository.findByUsername(username).orElseThrow(
                ()-> new UserNotFoundException(username)
        );

        String password= dto.password();
        String encodedPassword= userEntity.getPassword();

        token= jwtProvider.create(username);

        return new SignInResponseDto(token, 3600);
    }

    private static class CertificationNumber {

        public static String getCertificationNumber(){
            String certificationNumber= "";

            for (int count=0; count<4;count++) {
                certificationNumber += (int)(Math.random() * 10);
            }
            return certificationNumber;
        }
    }


}
