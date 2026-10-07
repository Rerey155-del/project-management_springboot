package com.nodewave.portal.feature.pm;

import com.nodewave.portal.core.entity.Project;

import java.time.LocalDateTime;

public record ProjectDto(
        String id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long taskCount
) {
    public static ProjectDto fromEntity(Project project, long taskCount) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                taskCount
        );
    }
}
