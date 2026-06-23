package com.microblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank(message = "Content is required")
        @Size(min = 1, max = 280, message = "Content must be between 1 and 280 characters")
        String content
) {}
