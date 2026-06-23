package com.microblog.dto;

public record FollowResponse(
        String message,
        String followerUsername,
        String followingUsername
) {}
