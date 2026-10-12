package com.nicolas.clouddrive.repository;

import com.nicolas.clouddrive.domain.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {

    Optional<FileVersion> findTopByFileIdOrderByVersionNumberDesc(UUID fileId);
}
