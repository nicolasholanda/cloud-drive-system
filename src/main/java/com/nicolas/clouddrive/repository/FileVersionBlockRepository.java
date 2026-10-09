package com.nicolas.clouddrive.repository;

import com.nicolas.clouddrive.domain.FileVersionBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileVersionBlockRepository extends JpaRepository<FileVersionBlock, UUID> {
}
