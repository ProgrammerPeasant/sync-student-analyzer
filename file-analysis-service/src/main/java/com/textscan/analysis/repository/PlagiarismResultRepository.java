package com.textscan.analysis.repository;

import com.textscan.analysis.model.PlagiarismResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlagiarismResultRepository extends JpaRepository<PlagiarismResult, UUID> {
    List<PlagiarismResult> findBySourceFileId(UUID sourceFileId);

    Optional<PlagiarismResult> findBySourceFileIdAndTargetFileId(UUID sourceFileId, UUID targetFileId);
}