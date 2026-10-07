package com.nodewave.portal.feature.pm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateDependencyRequest(
        @Schema(description = "ID tugas prasyarat yang harus diselesaikan terlebih dahulu", example = "task-001")
        @NotBlank(message = "dependsOnTaskId is required")
        String dependsOnTaskId
) {}
