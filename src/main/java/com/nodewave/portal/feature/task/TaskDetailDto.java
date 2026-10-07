package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.Task;
import com.nodewave.portal.core.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TaskDetailDto(
        String id,
        String title,
        String description,
        TaskStatus status,
        Boolean isClientVisible,
        Integer version,
        String attachmentUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        TaskAssigneeDto assignee,
        List<TaskDependencyItemDto> dependsOn
) {
    public static TaskDetailDto fromEntity(Task task, List<TaskDependencyItemDto> dependsOn) {
        return new TaskDetailDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getIsClientVisible(),
                task.getVersion(),
                task.getAttachmentUrl(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                TaskAssigneeDto.fromEntity(task.getAssignee()),
                dependsOn != null ? dependsOn : List.of()
        );
    }
}
