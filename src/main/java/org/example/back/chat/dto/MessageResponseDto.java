package org.example.back.chat.dto;

import java.util.List;

import org.example.back.rabbitmq.MessageDto;

public record MessageResponseDto(List<MessageDto> messages, boolean hasMore) {}
