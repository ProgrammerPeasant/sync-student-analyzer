package com.textscan.storage.service;

import com.textscan.storage.dto.FileDto;
import com.textscan.storage.exception.FileNotFoundException;
import com.textscan.storage.model.FileEntity;
import com.textscan.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileRepository fileRepository;

    public FileDto storeFile(MultipartFile file) throws IOException {
        FileEntity fileEntity = FileEntity.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .content(file.getBytes())
                .size(file.getSize())
                .build();

        FileEntity savedFile = fileRepository.save(fileEntity);
        log.info("File stored: {}", savedFile.getFileName());

        return convertToDto(savedFile);
    }

    public FileDto getFile(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
        return convertToDto(fileEntity);
    }

    public List<FileDto> getAllFiles() {
        return fileRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public byte[] getFileContent(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
        return fileEntity.getContent();
    }

    public void deleteFile(UUID id) {
        if (!fileRepository.existsById(id)) {
            throw new FileNotFoundException("File not found with id: " + id);
        }
        fileRepository.deleteById(id);
        log.info("File deleted: {}", id);
    }

    private FileDto convertToDto(FileEntity fileEntity) {
        return FileDto.builder()
                .id(fileEntity.getId())
                .fileName(fileEntity.getFileName())
                .contentType(fileEntity.getContentType())
                .size(fileEntity.getSize())
                .createdAt(fileEntity.getCreatedAt())
                .build();
    }
}