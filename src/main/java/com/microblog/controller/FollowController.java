package com.microblog.controller;

import com.microblog.dto.FollowRequest;
import com.microblog.dto.FollowResponse;
import com.microblog.dto.UserStatsResponse;
import com.microblog.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return 1L; // TODO: Получить реальный ID
    }

    // Подписаться
    @PostMapping
    public ResponseEntity<FollowResponse> followUser(@Valid @RequestBody FollowRequest request) {
        Long followerId = getCurrentUserId();
        return ResponseEntity.ok(followService.followUser(followerId, request));
    }

    // Отписаться
    @DeleteMapping("/{followingId}")
    public ResponseEntity<FollowResponse> unfollowUser(@PathVariable Long followingId) {
        Long followerId = getCurrentUserId();
        return ResponseEntity.ok(followService.unfollowUser(followerId, followingId));
    }

    // Проверить, подписан ли пользователь
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(followService.isFollowing(currentUserId, userId));
    }

    // Статистика пользователя
    @GetMapping("/stats/{userId}")
    public ResponseEntity<UserStatsResponse> getUserStats(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(followService.getUserStats(userId, currentUserId));
    }
}
