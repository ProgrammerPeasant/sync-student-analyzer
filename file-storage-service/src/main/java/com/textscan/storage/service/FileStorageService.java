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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    static final String NOT_FOUND_WITH_ID = "File not found with id: ";

    private final FileRepository fileRepository;

    public FileDto storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() == 0) {
            log.warn("Attempted to store an empty file: {}", file.getOriginalFilename());
            throw new IOException("Cannot store an empty file.");
        }

        String fileHash;
        try {
            fileHash = calculateFileHash(file.getBytes());
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calculating file hash: {}", e.getMessage());
            throw new IOException("Failed to calculate file hash.", e);
        }

        Optional<FileEntity> existingFile = fileRepository.findByFileHash(fileHash);

        if (existingFile.isPresent()) {
            log.info("File with hash {} already exists. Returning existing file: {}", fileHash, existingFile.get().getFileName());
            return convertToDto(existingFile.get());
        } else {
            FileEntity fileEntity = FileEntity.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .content(file.getBytes())
                    .size(file.getSize())
                    .fileHash(fileHash)
                    .build();

            FileEntity savedFile = fileRepository.save(fileEntity);
            log.info("New file stored: {} with hash {}", savedFile.getFileName(), fileHash);

            return convertToDto(savedFile);
        }
    }

    private String calculateFileHash(byte[] fileContent) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileContent);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public FileDto getFile(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(NOT_FOUND_WITH_ID + id));
        return convertToDto(fileEntity);
    }

    public List<FileDto> getAllFiles() {
        return fileRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public byte[] getFileContent(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(NOT_FOUND_WITH_ID + id));
        return fileEntity.getContent();
    }

    public void deleteFile(UUID id) {
        if (!fileRepository.existsById(id)) {
            throw new FileNotFoundException(NOT_FOUND_WITH_ID + id);
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