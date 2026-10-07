package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.Role;

import java.util.List;
import java.util.Map;

public record TasksResponse(
        Role role,
        Map<String, Object> meta,
        Map<String, Object> metrics,
        List<?> tasks
) {}
