package com.textscan.analysis.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AnalysisResultDto {
    private UUID id;
    private UUID fileId;
    private String fileName;
    private int paragraphCount;
    private int wordCount;
    private int characterCount;
    private String wordCloudUrl;
    private LocalDateTime createdAt;
}