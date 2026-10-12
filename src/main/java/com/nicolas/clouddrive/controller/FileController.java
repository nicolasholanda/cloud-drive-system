package com.nicolas.clouddrive.controller;

import com.nicolas.clouddrive.dto.file.FileDownloadResponse;
import com.nicolas.clouddrive.dto.file.FileUploadResponse;
import com.nicolas.clouddrive.service.FileDownloadService;
import com.nicolas.clouddrive.service.FileUploadService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;

    public FileController(FileUploadService fileUploadService, FileDownloadService fileDownloadService) {
        this.fileUploadService = fileUploadService;
        this.fileDownloadService = fileDownloadService;
    }

    @PostMapping
    public ResponseEntity<FileUploadResponse> upload(Authentication authentication,
                                                       @RequestParam String name,
                                                       @RequestParam MultipartFile file) {
        try {
            FileUploadResponse response = fileUploadService.upload(authentication.getName(), name, file.getInputStream());
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
}
