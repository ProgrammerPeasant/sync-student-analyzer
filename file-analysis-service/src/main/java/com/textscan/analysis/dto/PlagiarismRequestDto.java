package com.textscan.analysis.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PlagiarismRequestDto {
    private UUID sourceFileId;
    private UUID targetFileId;
}