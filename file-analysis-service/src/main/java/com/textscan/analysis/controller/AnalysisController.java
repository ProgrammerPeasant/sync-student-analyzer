package com.textscan.analysis.controller;

import com.textscan.analysis.dto.AnalysisResultDto;
import com.textscan.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis API", description = "API для анализа текстовых файлов")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/files/{fileId}")
    @Operation(summary = "Анализировать файл", description = "Выполняет анализ текстового файла")
    @ApiResponse(responseCode = "200", description = "Анализ успешно выполнен")
    public ResponseEntity<AnalysisResultDto> analyzeFile(@PathVariable UUID fileId) {
        AnalysisResultDto result = analysisService.analyzeFile(fileId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/files/{fileId}")
    @Operation(summary = "Получить результат анализа", description = "Возвращает результат анализа файла по ID")
    public ResponseEntity<AnalysisResultDto> getAnalysisResult(@PathVariable UUID fileId) {
        AnalysisResultDto result = analysisService.getAnalysisResult(fileId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Получить все результаты анализа", description = "Возвращает список всех результатов анализа")
    public ResponseEntity<List<AnalysisResultDto>> getAllAnalysisResults() {
        List<AnalysisResultDto> results = analysisService.getAllAnalysisResults();
        return ResponseEntity.ok(results);
    }
}