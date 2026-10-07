package com.nodewave.portal.feature.auth;

public record LoginResponse(
        String message,
        String token,
        UserDto user
) {}
