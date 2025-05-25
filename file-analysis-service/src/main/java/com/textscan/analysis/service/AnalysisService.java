package com.textscan.analysis.service;

import com.textscan.analysis.client.FileStorageClient;
import com.textscan.analysis.dto.AnalysisResultDto;
import com.textscan.analysis.dto.FileDto;
import com.textscan.analysis.exception.AnalysisException;
import com.textscan.analysis.exception.ResourceNotFoundException;
import com.textscan.analysis.model.AnalysisResult;
import com.textscan.analysis.repository.AnalysisResultRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final FileStorageClient fileStorageClient;
    private final AnalysisResultRepository analysisResultRepository;

    public AnalysisResultDto analyzeFile(UUID fileId) {
        try {
            Optional<AnalysisResult> existingResult = analysisResultRepository.findByFileId(fileId);
            if (existingResult.isPresent()) {
                return convertToDto(existingResult.get());
            }

            ResponseEntity<Resource> response = fileStorageClient.downloadFile(fileId);
            Resource fileResource = response.getBody();

            if (fileResource == null) {
                throw new ResourceNotFoundException("File not found with id: " + fileId);
            }

            log.info("Headers from storage-service: {}", response.getHeaders());

            String fileName = extractFileName(response);

            String content = readFileContent(fileResource);

            int paragraphCount = countParagraphs(content);
            int wordCount = countWords(content);
            int characterCount = countCharacters(content);

            String wordCloudUrl = generateWordCloudUrlUsingFrequencies(content);

            AnalysisResult result = AnalysisResult.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .paragraphCount(paragraphCount)
                    .wordCount(wordCount)
                    .characterCount(characterCount)
                    .wordCloudUrl(wordCloudUrl)
                    .build();

            AnalysisResult savedResult = analysisResultRepository.save(result);
            return convertToDto(savedResult);

        } catch (FeignException e) {
            log.error("Error communicating with file-storage-service", e);
            throw new AnalysisException("Error retrieving file from storage: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error reading file content", e);
            throw new AnalysisException("Error reading file content: " + e.getMessage());
        }
    }

    public AnalysisResultDto getAnalysisResult(UUID fileId) {
        AnalysisResult result = analysisResultRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis result not found for file: " + fileId));
        return convertToDto(result);
    }

    public List<AnalysisResultDto> getAllAnalysisResults() {
        return analysisResultRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private String readFileContent(Resource fileResource) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileResource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private int countParagraphs(String content) {
        String[] paragraphs = content.split("\\n\\s*\\n");
        return (int) Arrays.stream(paragraphs)
                .filter(p -> !p.trim().isEmpty())
                .count();
    }

    private int countWords(String content) {
        String[] words = content.split("\\s+|\\p{Punct}");
        return (int) Arrays.stream(words)
                .filter(word -> !word.trim().isEmpty())
                .count();
    }

    private int countCharacters(String content) {
        return (int) content.chars()
                .filter(c -> !Character.isWhitespace(c))
                .count();
    }

    private Map<String, Integer> calculateWordFrequency(String content) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = content.toLowerCase().split("\\s+|\\p{Punct}");

        for (String word : words) {
            word = word.trim();
            if (word.length() > 2) {  // я решил длиннее 2 букв
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }

        return wordFrequency;
    }

    private AnalysisResultDto convertToDto(AnalysisResult result) {
        return AnalysisResultDto.builder()
                .id(result.getId())
                .fileId(result.getFileId())
                .fileName(result.getFileName())
                .paragraphCount(result.getParagraphCount())
                .wordCount(result.getWordCount())
                .characterCount(result.getCharacterCount())
                .wordCloudUrl(result.getWordCloudUrl())
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

    private String generateWordCloudUrlUsingFrequencies(String content) {
        Map<String, Integer> wordFrequency = calculateWordFrequency(content);

        if (wordFrequency.isEmpty()) {
            return "";
        }

        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFrequency.entrySet());
        sortedWords.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        StringBuilder textParamBuilder = new StringBuilder();
        int currentLength = 0;
        final int MAX_TEXT_PARAM_LENGTH = 750;
        final int MAX_REPETITIONS_PER_WORD = 10;

        for (Map.Entry<String, Integer> entry : sortedWords) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            int repetitions = Math.min(frequency, MAX_REPETITIONS_PER_WORD);

            for (int i = 0; i < repetitions; i++) {
                if (currentLength + word.length() + 1 > MAX_TEXT_PARAM_LENGTH) {
                    break;
                }
                if (!textParamBuilder.isEmpty()) {
                    textParamBuilder.append(" ");
                    currentLength++;
                }
                textParamBuilder.append(word);
                currentLength += word.length();
            }

            if (currentLength >= MAX_TEXT_PARAM_LENGTH) {
                break;
            }
        }

        if (textParamBuilder.isEmpty()) {
            if (!sortedWords.isEmpty()) {
                String firstWord = sortedWords.get(0).getKey();
                if (firstWord.length() < MAX_TEXT_PARAM_LENGTH) {
                    textParamBuilder.append(firstWord);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }

        String encodedText = URLEncoder.encode(textParamBuilder.toString(), StandardCharsets.UTF_8);
        String baseUrl = "https://quickchart.io/wordcloud?text=";
        String finalUrl = baseUrl + encodedText;

        if (finalUrl.length() > 1023) {
            System.err.println("WARN: Generated word cloud URL is too long after encoding: " + finalUrl.length());
        }
        return finalUrl;

    }
}