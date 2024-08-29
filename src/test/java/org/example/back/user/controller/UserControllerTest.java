package org.example.back.user.controller;

import org.example.back.config.provider.JwtProvider;
import org.example.back.user.dto.User;
import org.example.back.user.dto.request.CheckCertificationRequestDto;
import org.example.back.user.dto.request.EmailCertificationRequestDto;
import org.example.back.user.dto.request.SignInRequestDto;
import org.example.back.user.dto.request.SignUpRequestDto;
import org.example.back.user.dto.response.CheckCertificationResponseDto;
import org.example.back.user.dto.response.EmailCertificationResponseDto;
import org.example.back.user.dto.response.SignInResponseDto;
import org.example.back.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    public void testEmailCertification() throws Exception {
        // given
        EmailCertificationResponseDto responseDto = new EmailCertificationResponseDto("성공", "이메일 요청이 성공하였습니다");
        when(userService.emailCertification(any(EmailCertificationRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/email-certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"email\": \"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("성공"))
                .andExpect(jsonPath("$.message").value("이메일 요청이 성공하였습니다"));
    }

    @Test
    public void testCheckCertificationNumber() throws Exception {
        // given
        CheckCertificationResponseDto responseDto = new CheckCertificationResponseDto("성공", "인증에 성공했습니다");
        when(userService.checkCertificationNumber(any(CheckCertificationRequestDto.class))).thenReturn(responseDto);

        // 요청 본문에 id 필드 추가
        mockMvc.perform(post("/api/v1/auth/check-certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"someId\", \"email\": \"test@example.com\", \"certificationNumber\": \"1234\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSignUp() throws Exception {
        // given
        User user = new User("testUser", "test@example.com");
        when(userService.signUp(any(SignUpRequestDto.class))).thenReturn(user);

        // when & then
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"email\": \"test@example.com\", \"password\": \"password123\", \"certificationNumber\": \"1234\"}")) // Ensure password matches the pattern and add certificationNumber
                .andExpect(status().isOk());



    }

    @Test
    public void testSignIn() throws Exception {
        // given
        SignInResponseDto responseDto = new SignInResponseDto("mockedToken", 3600);
        when(userService.signIn(any(SignInRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"password\": \"password\"}"))
                .andExpect(status().isOk());

    }
}
