package org.example.back;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.back.config.provider.EmailProvider;
import org.example.back.config.provider.JwtProvider;
import org.example.back.profile.repository.ProfileRepository;
import org.example.back.profile.service.ProfileService;
import org.example.back.user.repository.CertificationRepository;
import org.example.back.user.repository.UserRepository;
import org.example.back.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
class BackApplicationTests {



    @Test
    void contextLoads() {
    }


}