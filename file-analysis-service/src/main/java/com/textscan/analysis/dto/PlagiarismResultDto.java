package com.textscan.analysis.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PlagiarismResultDto {
    private UUID id;
    private UUID sourceFileId;
    private UUID targetFileId;
    private String sourceFileName;
    private String targetFileName;
    private double similarityPercentage;
    private boolean isPlagiarism;
    private LocalDateTime createdAt;
}