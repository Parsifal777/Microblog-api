package com.microblog.controller;

import com.microblog.dto.FeedResponse;
import com.microblog.dto.PostRequest;
import com.microblog.dto.PostResponse;
import com.microblog.service.PostService;
import com.microblog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    // Создать пост
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(postService.createPost(userId, request));
    }

    // Получить пост по ID
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(postService.getPostById(postId, userId));
    }

    // Получить все посты пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = userService.getCurrentUserId();
        return ResponseEntity.ok(postService.getUserPosts(userId, currentUserId, page, size));
    }

    // Обновить пост
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(postService.updatePost(postId, userId, request));
    }

    // Удалить пост
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        Long userId = userService.getCurrentUserId();
        postService.deletePost(postId, userId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    // Лента постов
    @GetMapping("/feed")
    public ResponseEntity<FeedResponse> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userService.getCurrentUserId();
        Page<PostResponse> posts = postService.getFeed(userId, page, size);
        FeedResponse response = new FeedResponse(
                posts.getContent(),
                posts.getNumber(),
                posts.getTotalPages(),
                posts.getTotalElements(),
                posts.getSize()
        );
        return ResponseEntity.ok(response);
    }
}
