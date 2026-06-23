package com.microblog.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String content,
        String username,
        Integer likesCount,
        LocalDateTime createdAt
) {}
