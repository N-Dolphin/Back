package org.example.back.rabbitmq;

public record ApiResponse(String result, int resultCode, String resultMsg) {}
