package com.nodewave.portal.feature.auth;

import com.nodewave.portal.core.entity.Department;
import com.nodewave.portal.core.entity.Role;
import com.nodewave.portal.core.entity.User;

public record UserDto(
        String id,
        String name,
        String email,
        Role role,
        Department department
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment()
        );
    }
}
