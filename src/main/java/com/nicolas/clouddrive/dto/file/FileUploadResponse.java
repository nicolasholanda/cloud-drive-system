package com.nicolas.clouddrive.dto.file;

import java.util.UUID;

public record FileUploadResponse(UUID fileId, UUID versionId, Integer versionNumber, Long totalSizeBytes) {
}
