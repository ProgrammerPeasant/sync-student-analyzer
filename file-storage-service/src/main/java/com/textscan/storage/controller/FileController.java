package com.textscan.storage.controller;

import com.textscan.storage.dto.FileDto;
import com.textscan.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Storage API", description = "API для управления файлами")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Загрузить файл", description = "Загружает файл в хранилище")
    @ApiResponse(responseCode = "200", description = "Файл успешно загружен")
    public ResponseEntity<FileDto> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        FileDto fileDto = fileStorageService.storeFile(file);
        return ResponseEntity.ok(fileDto);
    }

    @GetMapping
    @Operation(summary = "Получить все файлы", description = "Возвращает список всех файлов")
    public ResponseEntity<List<FileDto>> getAllFiles() {
        List<FileDto> files = fileStorageService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить информацию о файле", description = "Возвращает информацию о файле по ID")
    public ResponseEntity<FileDto> getFile(@PathVariable UUID id) {
        FileDto fileDto = fileStorageService.getFile(id);
        return ResponseEntity.ok(fileDto);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Скачать файл", description = "Скачивает файл по ID")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        FileDto fileDto = fileStorageService.getFile(id);
        byte[] content = fileStorageService.getFileContent(id);

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDto.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить файл", description = "Удаляет файл по ID")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileStorageService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}