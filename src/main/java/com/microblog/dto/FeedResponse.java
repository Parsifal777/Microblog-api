package com.microblog.dto;

import java.util.List;

public record FeedResponse(
        List<PostResponse> posts,
        int currentPage,
        int totalPages,
        long totalElements,
        int pageSize
) {}
