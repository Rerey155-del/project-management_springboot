package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.TaskDependency;

public record TaskDependencyItemDto(
        String id,
        TaskPrerequisiteDto dependsOnTask
) {
    public static TaskDependencyItemDto fromEntity(TaskDependency dep) {
        if (dep == null) return null;
        return new TaskDependencyItemDto(
                dep.getId(),
                TaskPrerequisiteDto.fromEntity(dep.getDependsOnTask())
        );
    }
}
