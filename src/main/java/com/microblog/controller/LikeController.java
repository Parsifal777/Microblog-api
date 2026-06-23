package com.microblog.controller;

import com.microblog.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return 1L; // TODO: Получить реальный ID
    }

    // Поставить лайк
    @PostMapping("/{postId}")
    public ResponseEntity<String> likePost(@PathVariable Long postId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(likeService.likePost(userId, postId));
    }

    // Убрать лайк
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> unlikePost(@PathVariable Long postId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(likeService.unlikePost(userId, postId));
    }

    // Проверить, поставил ли пользователь лайк
    @GetMapping("/check/{postId}")
    public ResponseEntity<Boolean> isLiked(@PathVariable Long postId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(likeService.isLiked(userId, postId));
    }

    // Получить количество лайков у поста
    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikesCount(postId));
    }
}
