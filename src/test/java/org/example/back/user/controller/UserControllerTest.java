package org.example.back.user.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)

public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    public void testEmailCertification() throws Exception {
        // given
        EmailCertificationResponseDto responseDto = new EmailCertificationResponseDto("성공", "이메일 요청이 성공하였습니다");
        when(userService.emailCertification(any(EmailCertificationRequestDto.class))).thenReturn(responseDto);

        EmailCertificationRequestDto requestDto = new EmailCertificationRequestDto("testUser", "test@example.com");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/email-certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("성공"))
                .andExpect(jsonPath("$.message").value("이메일 요청이 성공하였습니다"));
    }

    @Test
    public void testCheckCertificationNumber() throws Exception {
        // given
        CheckCertificationResponseDto responseDto = new CheckCertificationResponseDto("성공", "인증에 성공했습니다");
        when(userService.checkCertificationNumber(any(CheckCertificationRequestDto.class))).thenReturn(responseDto);

        CheckCertificationRequestDto requestDto = new CheckCertificationRequestDto("someId", "test@example.com", "1234");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/check-certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("성공"))
                .andExpect(jsonPath("$.message").value("인증에 성공했습니다"));
    }

    @Test
    public void testSignUp() throws Exception {
        // given
        User user = new User("testUser", "test@example.com");
        when(userService.signUp(any(SignUpRequestDto.class))).thenReturn(user);

        SignUpRequestDto requestDto = new SignUpRequestDto("testUser", "test@example.com", "password123", "1234");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    public void testSignIn() throws Exception {
        // given
        SignInResponseDto responseDto = new SignInResponseDto("mockedToken", 3600);
        when(userService.signIn(any(SignInRequestDto.class))).thenReturn(responseDto);

        SignInRequestDto requestDto = new SignInRequestDto("testUser", "password");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
