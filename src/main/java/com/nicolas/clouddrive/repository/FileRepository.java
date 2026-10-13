package com.nicolas.clouddrive.repository;

import com.nicolas.clouddrive.domain.FileEntity;
import com.nicolas.clouddrive.domain.enums.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    List<FileEntity> findByOwnerIdAndStatus(UUID ownerId, FileStatus status);
}
