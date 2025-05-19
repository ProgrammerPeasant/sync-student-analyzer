package com.textscan.analysis.controller;

import com.textscan.analysis.dto.PlagiarismRequestDto;
import com.textscan.analysis.dto.PlagiarismResultDto;
import com.textscan.analysis.service.PlagiarismService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis/plagiarism")
@RequiredArgsConstructor
@Tag(name = "Plagiarism API", description = "API для проверки плагиата")
public class PlagiarismController {

    private final PlagiarismService plagiarismService;

    @PostMapping("/check")
    @Operation(summary = "Проверить на плагиат", description = "Проверяет два файла на плагиат")
    @ApiResponse(responseCode = "200", description = "Проверка успешно выполнена")
    public ResponseEntity<PlagiarismResultDto> checkPlagiarism(@RequestBody PlagiarismRequestDto request) {
        PlagiarismResultDto result = plagiarismService.checkPlagiarism(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/files/{fileId}")
    @Operation(summary = "Получить результаты проверки на плагиат",
            description = "Возвращает результаты проверки на плагиат для файла")
    public ResponseEntity<List<PlagiarismResultDto>> getPlagiarismResults(@PathVariable UUID fileId) {
        List<PlagiarismResultDto> results = plagiarismService.getPlagiarismResults(fileId);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    @Operation(summary = "Получить все результаты проверки на плагиат",
            description = "Возвращает список всех результатов проверки на плагиат")
    public ResponseEntity<List<PlagiarismResultDto>> getAllPlagiarismResults() {
        List<PlagiarismResultDto> results = plagiarismService.getAllPlagiarismResults();
        return ResponseEntity.ok(results);
    }
}