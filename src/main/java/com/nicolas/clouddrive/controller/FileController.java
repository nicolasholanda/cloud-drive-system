package com.nicolas.clouddrive.controller;

import com.nicolas.clouddrive.dto.file.FileUploadResponse;
import com.nicolas.clouddrive.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileUploadService fileUploadService;

    public FileController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
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
}
