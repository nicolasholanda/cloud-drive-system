package com.nicolas.clouddrive.controller;

import com.nicolas.clouddrive.dto.file.FileDownloadResponse;
import com.nicolas.clouddrive.dto.file.FileSummaryResponse;
import com.nicolas.clouddrive.dto.file.FileUploadResponse;
import com.nicolas.clouddrive.service.FileDownloadService;
import com.nicolas.clouddrive.service.FileManagementService;
import com.nicolas.clouddrive.service.FileUploadService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;
    private final FileManagementService fileManagementService;

    public FileController(FileUploadService fileUploadService,
                           FileDownloadService fileDownloadService,
                           FileManagementService fileManagementService) {
        this.fileUploadService = fileUploadService;
        this.fileDownloadService = fileDownloadService;
        this.fileManagementService = fileManagementService;
    }

    @PostMapping
    public ResponseEntity<FileUploadResponse> upload(Authentication authentication,
                                                       @RequestParam String name,
                                                       @RequestParam MultipartFile file) {
        try {
            FileUploadResponse response = fileUploadService.uploadNewFile(authentication.getName(), name, file.getInputStream());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not read uploaded file", e);
        }
    }

    @PostMapping("/{fileId}/versions")
    public ResponseEntity<FileUploadResponse> uploadNewVersion(Authentication authentication,
                                                                 @PathVariable UUID fileId,
                                                                 @RequestParam MultipartFile file) {
        try {
            FileUploadResponse response =
                    fileUploadService.uploadNewVersion(authentication.getName(), fileId, file.getInputStream());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not read uploaded file", e);
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> download(Authentication authentication, @PathVariable UUID fileId) {
        FileDownloadResponse response = fileDownloadService.download(authentication.getName(), fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(response.fileName()).build().toString())
                .body(response.content());
    }

    @GetMapping
    public ResponseEntity<List<FileSummaryResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(fileManagementService.listFiles(authentication.getName()));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID fileId) {
        fileManagementService.deleteFile(authentication.getName(), fileId);
        return ResponseEntity.noContent().build();
    }
}
