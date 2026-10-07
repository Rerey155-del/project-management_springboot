package com.nodewave.portal.feature.internal;

import com.nodewave.portal.core.entity.TaskStatus;

public record UpdateTaskRequest(
        TaskStatus status,
        String description,
        Integer version
) {}
