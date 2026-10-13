package com.nicolas.clouddrive.service;

import com.nicolas.clouddrive.domain.Block;
import com.nicolas.clouddrive.domain.FileEntity;
import com.nicolas.clouddrive.domain.FileVersion;
import com.nicolas.clouddrive.domain.FileVersionBlock;
import com.nicolas.clouddrive.domain.User;
import com.nicolas.clouddrive.domain.enums.FileStatus;
import com.nicolas.clouddrive.dto.file.FileUploadResponse;
import com.nicolas.clouddrive.repository.BlockRepository;
import com.nicolas.clouddrive.repository.FileRepository;
import com.nicolas.clouddrive.repository.FileVersionBlockRepository;
import com.nicolas.clouddrive.repository.FileVersionRepository;
import com.nicolas.clouddrive.repository.UserRepository;
import com.nicolas.clouddrive.storage.BlockHasher;
import com.nicolas.clouddrive.storage.BlockSplitter;
import com.nicolas.clouddrive.storage.BlockStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FileVersionBlockRepository fileVersionBlockRepository;
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final BlockStorageService blockStorageService;
    private final BlockSplitter blockSplitter;
    private final BlockHasher blockHasher;

    public FileUploadService(FileRepository fileRepository,
                              FileVersionRepository fileVersionRepository,
                              FileVersionBlockRepository fileVersionBlockRepository,
                              BlockRepository blockRepository,
                              UserRepository userRepository,
                              BlockStorageService blockStorageService,
                              BlockSplitter blockSplitter,
                              BlockHasher blockHasher) {
        this.fileRepository = fileRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.fileVersionBlockRepository = fileVersionBlockRepository;
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.blockStorageService = blockStorageService;
        this.blockSplitter = blockSplitter;
        this.blockHasher = blockHasher;
    }

    public FileUploadResponse uploadNewFile(String ownerEmail, String fileName, InputStream content) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid user"));

        Instant now = Instant.now();

        FileEntity file = fileRepository.save(FileEntity.builder()
                .owner(owner)
                .name(fileName)
                .status(FileStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());

        return uploadVersion(file, 1, content);
    }

    public FileUploadResponse uploadNewVersion(String requesterEmail, UUID fileId, InputStream content) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));

        if (!file.getOwner().getEmail().equals(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not allowed to modify this file");
        }

        int previousVersionNumber = fileVersionRepository.findTopByFileIdOrderByVersionNumberDesc(fileId)
                .map(FileVersion::getVersionNumber)
                .orElse(0);

        file.setUpdatedAt(Instant.now());
        fileRepository.save(file);

        return uploadVersion(file, previousVersionNumber + 1, content);
    }

    private FileUploadResponse uploadVersion(FileEntity file, int versionNumber, InputStream content) {
        List<byte[]> blocks;
        try {
            blocks = blockSplitter.split(content);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not read uploaded file", e);
        }

        Instant now = Instant.now();
        long totalSizeBytes = blocks.stream().mapToLong(block -> block.length).sum();

        FileVersion fileVersion = fileVersionRepository.save(FileVersion.builder()
                .file(file)
                .versionNumber(versionNumber)
                .totalSizeBytes(totalSizeBytes)
                .createdAt(now)
                .build());

        List<FileVersionBlock> fileVersionBlocks = new ArrayList<>();

        for (int order = 0; order < blocks.size(); order++) {
            byte[] blockContent = blocks.get(order);
            String hash = blockHasher.hash(blockContent);

            Block block = blockRepository.findByHash(hash).orElseGet(() -> {
                blockStorageService.uploadBlock(hash, blockContent);
                return blockRepository.save(Block.builder()
                        .hash(hash)
                        .sizeBytes((long) blockContent.length)
                        .storageKey(hash)
                        .createdAt(now)
                        .build());
            });

            fileVersionBlocks.add(FileVersionBlock.builder()
                    .fileVersion(fileVersion)
                    .block(block)
                    .blockOrder(order)
                    .build());
        }

        fileVersionBlockRepository.saveAll(fileVersionBlocks);

        return new FileUploadResponse(file.getId(), fileVersion.getId(), fileVersion.getVersionNumber(), totalSizeBytes);
    }
}
