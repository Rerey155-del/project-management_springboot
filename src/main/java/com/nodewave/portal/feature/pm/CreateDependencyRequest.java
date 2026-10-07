package com.nodewave.portal.feature.pm;

import jakarta.validation.constraints.NotBlank;

public record CreateDependencyRequest(
        @NotBlank(message = "dependsOnTaskId is required")
        String dependsOnTaskId
) {}
