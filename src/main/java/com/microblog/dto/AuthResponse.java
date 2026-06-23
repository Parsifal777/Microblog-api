package com.microblog.dto;

public record AuthResponse(
        String token,
        String username,
        String message
) {}
