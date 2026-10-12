package com.nicolas.clouddrive.service;

import com.nicolas.clouddrive.domain.FileEntity;
import com.nicolas.clouddrive.domain.FileVersion;
import com.nicolas.clouddrive.domain.FileVersionBlock;
import com.nicolas.clouddrive.dto.file.FileDownloadResponse;
import com.nicolas.clouddrive.repository.FileRepository;
import com.nicolas.clouddrive.repository.FileVersionBlockRepository;
import com.nicolas.clouddrive.repository.FileVersionRepository;
import com.nicolas.clouddrive.storage.BlockStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
public class FileDownloadService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FileVersionBlockRepository fileVersionBlockRepository;
    private final BlockStorageService blockStorageService;

    public FileDownloadService(FileRepository fileRepository,
                                FileVersionRepository fileVersionRepository,
                                FileVersionBlockRepository fileVersionBlockRepository,
                                BlockStorageService blockStorageService) {
        this.fileRepository = fileRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.fileVersionBlockRepository = fileVersionBlockRepository;
        this.blockStorageService = blockStorageService;
    }

    public FileDownloadResponse download(String requesterEmail, UUID fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));

        if (!file.getOwner().getEmail().equals(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not allowed to access this file");
        }

        FileVersion latestVersion = fileVersionRepository.findTopByFileIdOrderByVersionNumberDesc(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file has no uploaded versions"));

        List<FileVersionBlock> orderedBlocks =
                fileVersionBlockRepository.findByFileVersionIdOrderByBlockOrderAsc(latestVersion.getId());

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        for (FileVersionBlock fileVersionBlock : orderedBlocks) {
            content.writeBytes(blockStorageService.downloadBlock(fileVersionBlock.getBlock().getStorageKey()));
        }

        return new FileDownloadResponse(file.getName(), content.toByteArray());
    }
}
