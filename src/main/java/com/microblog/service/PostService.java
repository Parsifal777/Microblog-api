package com.microblog.service;

import com.microblog.dto.PostRequest;
import com.microblog.dto.PostResponse;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.repository.LikeRepository;
import com.microblog.repository.PostRepository;
import com.microblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public PostResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(request.content());
        post.setUser(user);

        Post saved = postRepository.save(post);
        return toResponse(saved, userId);
    }

    // Используем JOIN FETCH для получения поста с пользователем
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            throw new RuntimeException("Post has been deleted");
        }

        return toResponse(post, currentUserId);
    }

    // Используем EntityGraph для JOIN FETCH
    public List<PostResponse> getUserPosts(Long userId, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return posts.stream()
                .map(post -> toResponse(post, currentUserId))
                .toList();
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        post.setDeleted(true);
        postRepository.save(post);
    }

    @Transactional
    public PostResponse updatePost(Long postId, Long userId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own posts");
        }

        post.setContent(request.content());
        Post updated = postRepository.save(post);

        return toResponse(updated, userId);
    }

    // Лента с JOIN FETCH
    public Page<PostResponse> getFeed(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findFeedPosts(userId, pageable);
        return posts.map(post -> toResponse(post, userId));
    }

    // Конвертация в DTO (уже использует данные из JOIN FETCH)
    private PostResponse toResponse(Post post, Long currentUserId) {
        boolean isLiked = likeRepository.existsByUserIdAndPostIdAndDeletedFalse(currentUserId, post.getId());
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getUser().getUsername(),  // User уже загружен через JOIN FETCH
                post.getUser().getId(),
                post.getLikesCount(),
                isLiked,
                post.getCreatedAt()
        );
    }
}
