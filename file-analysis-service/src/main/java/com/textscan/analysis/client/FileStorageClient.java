package com.textscan.analysis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "file-storage-service")
public interface FileStorageClient {

    @GetMapping("/api/files/{id}/download")
    ResponseEntity<Resource> downloadFile(@PathVariable UUID id);
}