package com.textscan.analysis.service;

import com.textscan.analysis.client.FileStorageClient;
import com.textscan.analysis.dto.PlagiarismRequestDto;
import com.textscan.analysis.dto.PlagiarismResultDto;
import com.textscan.analysis.exception.AnalysisException;
import com.textscan.analysis.exception.ResourceNotFoundException;
import com.textscan.analysis.model.PlagiarismResult;
import com.textscan.analysis.repository.PlagiarismResultRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlagiarismService {

    private final FileStorageClient fileStorageClient;
    private final PlagiarismResultRepository plagiarismResultRepository;

    public PlagiarismResultDto checkPlagiarism(PlagiarismRequestDto request) {
        UUID sourceFileId = request.getSourceFileId();
        UUID targetFileId = request.getTargetFileId();

        // Проверяем, был ли уже проведен анализ плагиата между этими файлами
        Optional<PlagiarismResult> existingResult =
                plagiarismResultRepository.findBySourceFileIdAndTargetFileId(sourceFileId, targetFileId);
        if (existingResult.isPresent()) {
            return convertToDto(existingResult.get());
        }

        try {
            String sourceContent = getFileContent(sourceFileId);
            String targetContent = getFileContent(targetFileId);

            String sourceFileName = extractFileName(fileStorageClient.downloadFile(sourceFileId));
            String targetFileName = extractFileName(fileStorageClient.downloadFile(targetFileId));

            double similarityPercentage = calculateSimilarity(sourceContent, targetContent);
            boolean isPlagiarism = similarityPercentage == 100.0; // Считаем плагиатом только при 100% совпадении

            PlagiarismResult result = PlagiarismResult.builder()
                    .sourceFileId(sourceFileId)
                    .targetFileId(targetFileId)
                    .sourceFileName(sourceFileName)
                    .targetFileName(targetFileName)
                    .similarityPercentage(similarityPercentage)
                    .isPlagiarism(isPlagiarism)
                    .build();

            PlagiarismResult savedResult = plagiarismResultRepository.save(result);
            return convertToDto(savedResult);

        } catch (FeignException e) {
            log.error("Error communicating with file-storage-service", e);
            throw new AnalysisException("Error retrieving file from storage: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error reading file content", e);
            throw new AnalysisException("Error reading file content: " + e.getMessage());
        }
    }

    public List<PlagiarismResultDto> getPlagiarismResults(UUID fileId) {
        List<PlagiarismResult> results = plagiarismResultRepository.findBySourceFileId(fileId);
        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PlagiarismResultDto> getAllPlagiarismResults() {
        return plagiarismResultRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private String getFileContent(UUID fileId) throws IOException {
        ResponseEntity<Resource> response = fileStorageClient.downloadFile(fileId);
        Resource fileResource = response.getBody();

        if (fileResource == null) {
            throw new ResourceNotFoundException("File not found with id: " + fileId);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileResource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private double calculateSimilarity(String source, String target) {
        // Простой алгоритм для определения 100% плагиата
        // В реальном проекте стоит использовать более сложные алгоритмы,
        // например, алгоритм Левенштейна или косинусное сходство

        // Нормализуем тексты перед сравнением (убираем лишние пробелы, переносы строки и т.д.)
        String normalizedSource = source.replaceAll("\\s+", " ").trim().toLowerCase();
        String normalizedTarget = target.replaceAll("\\s+", " ").trim().toLowerCase();

        if (normalizedSource.equals(normalizedTarget)) {
            return 100.0; // Полное совпадение
        }

        // Простое сравнение на основе количества общих слов
        String[] sourceWords = normalizedSource.split("\\s+");
        String[] targetWords = normalizedTarget.split("\\s+");

        int commonWords = 0;
        for (String sourceWord : sourceWords) {
            for (String targetWord : targetWords) {
                if (sourceWord.equals(targetWord)) {
                    commonWords++;
                    break;
                }
            }
        }

        int totalWords = Math.max(sourceWords.length, targetWords.length);
        return totalWords > 0 ? (double) commonWords / totalWords * 100 : 0;
    }

    private PlagiarismResultDto convertToDto(PlagiarismResult result) {
        return PlagiarismResultDto.builder()
                .id(result.getId())
                .sourceFileId(result.getSourceFileId())
                .targetFileId(result.getTargetFileId())
                .sourceFileName(result.getSourceFileName())
                .targetFileName(result.getTargetFileName())
                .similarityPercentage(result.getSimilarityPercentage())
                .isPlagiarism(result.isPlagiarism())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private String extractFileName(ResponseEntity<Resource> response) {
        String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            return contentDisposition.split("filename=\"")[1].split("\"")[0];
        }
        return "unknown-file";
    }
}