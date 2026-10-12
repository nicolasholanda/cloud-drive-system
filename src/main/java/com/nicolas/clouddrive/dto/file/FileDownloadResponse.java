package com.nicolas.clouddrive.dto.file;

public record FileDownloadResponse(String fileName, byte[] content) {
}
