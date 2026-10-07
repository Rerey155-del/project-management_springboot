package com.nodewave.portal.feature.internal;

import com.nodewave.portal.core.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTaskRequest(
        @Schema(description = "Status baru tugas", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "Deskripsi baru (hanya PM yang berhak mengedit)", example = "Update deskripsi spesifikasi")
        String description,

        @Schema(description = "Versi data untuk Optimistic Locking", example = "0")
        Integer version
) {}
