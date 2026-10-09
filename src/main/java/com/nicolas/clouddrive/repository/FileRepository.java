package com.nicolas.clouddrive.repository;

import com.nicolas.clouddrive.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
}
