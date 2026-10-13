package com.nicolas.clouddrive.service;

import com.nicolas.clouddrive.domain.FileEntity;
import com.nicolas.clouddrive.domain.User;
import com.nicolas.clouddrive.domain.enums.FileStatus;
import com.nicolas.clouddrive.dto.file.FileSummaryResponse;
import com.nicolas.clouddrive.repository.FileRepository;
import com.nicolas.clouddrive.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileManagementService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileManagementService(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public List<FileSummaryResponse> listFiles(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid user"));

        return fileRepository.findByOwnerIdAndStatus(owner.getId(), FileStatus.ACTIVE).stream()
                .map(file -> new FileSummaryResponse(file.getId(), file.getName(), file.getCreatedAt(), file.getUpdatedAt()))
                .toList();
    }

    public void deleteFile(String requesterEmail, UUID fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));

        if (!file.getOwner().getEmail().equals(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not allowed to modify this file");
        }

        file.setStatus(FileStatus.DELETED);
        file.setUpdatedAt(Instant.now());
        fileRepository.save(file);
    }
}
