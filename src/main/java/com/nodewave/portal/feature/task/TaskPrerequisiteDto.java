package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.Task;
import com.nodewave.portal.core.entity.TaskStatus;

public record TaskPrerequisiteDto(
        String id,
        String title,
        TaskStatus status
) {
    public static TaskPrerequisiteDto fromEntity(Task task) {
        if (task == null) return null;
        return new TaskPrerequisiteDto(task.getId(), task.getTitle(), task.getStatus());
    }
}
