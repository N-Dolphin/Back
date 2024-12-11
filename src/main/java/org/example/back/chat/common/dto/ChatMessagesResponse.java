package org.example.back.chat.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessagesResponse {
	private List<MessageRes> messages;
	private int currentPage;
	private int totalPages;
	private boolean hasNext;
	private long totalElements;
}