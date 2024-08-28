package org.example.back.user.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record SignInResponseDto(

        @Schema(description = "엑세스토큰", example = "QWERASDFZXVTYUIGHJKBNM")
        String token,

        @Schema(description = "만료시간")
        int expiredTime

){}

