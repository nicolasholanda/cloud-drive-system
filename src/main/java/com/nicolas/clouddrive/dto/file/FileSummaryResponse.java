package com.nicolas.clouddrive.dto.file;

import java.time.Instant;
import java.util.UUID;

public record FileSummaryResponse(UUID id, String name, Instant createdAt, Instant updatedAt) {
}
