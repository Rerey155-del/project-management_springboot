package com.nodewave.portal.feature.client;

import com.nodewave.portal.core.entity.Task;
import com.nodewave.portal.core.entity.TaskStatus;

import java.time.LocalDateTime;

public record ClientTaskDto(
        String id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt
) {
    public static ClientTaskDto fromEntity(Task task) {
        return new ClientTaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}
