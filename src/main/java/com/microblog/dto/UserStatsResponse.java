package com.microblog.dto;

public record UserStatsResponse(
        Long postsCount,
        Long followersCount,
        Long followingCount,
        Boolean isFollowedByCurrentUser
) {}
