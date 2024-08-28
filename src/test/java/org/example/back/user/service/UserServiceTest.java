package org.example.back.user.service;

import org.example.back.config.provider.EmailProvider;
import org.example.back.config.provider.JwtProvider;
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
import org.springframework.context.annotation.Import;

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
    private JwtProvider jwtProvider;

    @Autowired
    private UserService userService;

    @Test
    public void testSignInSuccess() {
        // given
        String username = "testUser";
        String password = "password";
        SignInRequestDto signInRequestDto = new SignInRequestDto(username, password);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(jwtProvider.create(username)).thenReturn("mockedToken");

        // when
        SignInResponseDto responseDto = userService.signIn(signInRequestDto);

        // then
        assertEquals("mockedToken", responseDto.token());
    }

    @Test
    public void testSignInUserNotFound() {
        // given
        String username = "nonExistentUser";
        SignInRequestDto signInRequestDto = new SignInRequestDto(username, "password");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when / then
        assertThrows(UserNotFoundException.class, () -> {
            userService.signIn(signInRequestDto);
        });
    }

}
