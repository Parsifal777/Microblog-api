package com.microblog.dto;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(
        @NotNull(message = "Following user ID is required")
        Long followingId
) {}
