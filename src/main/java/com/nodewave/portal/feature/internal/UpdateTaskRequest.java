package com.nodewave.portal.feature.internal;

import com.nodewave.portal.core.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTaskRequest(
        @Schema(description = "Status baru tugas", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "Deskripsi baru (Hanya dapat diubah oleh PM, kosongkan jika engineer)", example = "")
        String description,

        @Schema(description = "Versi data untuk Optimistic Locking (Awal: 1)", example = "1")
        Integer version
) {}
