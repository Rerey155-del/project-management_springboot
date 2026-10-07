package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.Department;
import com.nodewave.portal.core.entity.User;

public record TaskAssigneeDto(
        String id,
        String name,
        Department department
) {
    public static TaskAssigneeDto fromEntity(User user) {
        if (user == null) return null;
        return new TaskAssigneeDto(user.getId(), user.getName(), user.getDepartment());
    }
}
