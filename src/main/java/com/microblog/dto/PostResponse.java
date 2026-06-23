package com.microblog.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String content,
        String username,
        Long userId,
        Integer likesCount,
        Boolean isLikedByCurrentUser,
        LocalDateTime createdAt
) {}
