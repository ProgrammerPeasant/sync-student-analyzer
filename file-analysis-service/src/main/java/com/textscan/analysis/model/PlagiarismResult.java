package com.textscan.analysis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plagiarism_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlagiarismResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sourceFileId;

    @Column(nullable = false)
    private UUID targetFileId;

    @Column(nullable = false)
    private String sourceFileName;

    @Column(nullable = false)
    private String targetFileName;

    @Column(nullable = false)
    private double similarityPercentage;

    @Column(nullable = false)
    private boolean isPlagiarism;

    @CreationTimestamp
    private LocalDateTime createdAt;
}