package org.example.back.user.service;

import org.example.back.config.provider.AuthTokens;
import org.example.back.config.provider.AuthTokensGenerator;
import org.example.back.config.provider.EmailProvider;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.response.SignInResponseDto;
import org.example.back.user.entity.UserEntity;
import org.example.back.user.exception.UserNotFoundException;
import org.example.back.user.repository.CertificationRepository;
import org.example.back.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailProvider emailProvider;

    @MockBean
    private CertificationRepository certificationRepository;

    @MockBean
    private ProfileRepository profileRepository;


    @MockBean
    private AuthTokensGenerator authTokensGenerator;

    @Autowired
    private UserService userService;

    @Test
    public void testSignInSuccess() {
        // given
        String email = "test1234@naver.com";
        String password = "password";
        SignInRequestDto signInRequestDto = new SignInRequestDto(email,password);

        UserEntity userEntity = new UserEntity();
        userEntity.setPassword(password);
        userEntity.setUserId(1L); // 유저 ID 설정

        AuthTokens mockedTokens = AuthTokens.of("mockedAccessToken", "mockedRefreshToken", "Bearer", 3600L);

        // Mocking repository and generator behavior
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(authTokensGenerator.generate(userEntity.getUserId())).thenReturn(mockedTokens);

        // when
        SignInResponseDto responseDto = userService.signIn(signInRequestDto);

        // then
        assertEquals(3600, responseDto.expiredTime());
    }

    @Test
    public void testSignInUserNotFound() {
        // given
        String email = "test1234";
        SignInRequestDto signInRequestDto = new SignInRequestDto(email, "password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when / then
        assertThrows(UserNotFoundException.class, () -> {
            userService.signIn(signInRequestDto);
        });
    }
}
