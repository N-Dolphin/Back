package org.example.back.chat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {
	private String fileName;
	private String fileUrl;
	private String fileType;
	private Integer fileSize;
	private String thumbnailUrl;
}