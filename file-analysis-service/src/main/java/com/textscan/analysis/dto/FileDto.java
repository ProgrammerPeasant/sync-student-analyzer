package com.textscan.analysis.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FileDto {
    private UUID id;
    private String fileName;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
}