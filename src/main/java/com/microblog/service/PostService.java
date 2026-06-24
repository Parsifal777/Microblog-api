package com.microblog.service;

import com.microblog.dto.PostRequest;
import com.microblog.dto.PostResponse;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.repository.LikeRepository;
import com.microblog.repository.PostRepository;
import com.microblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional
    @CacheEvict(value = "feed", allEntries = true)  // ❗ При создании поста — очищаем кэш ленты
    public PostResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(request.content());
        post.setUser(user);

        Post saved = postRepository.save(post);
        log.info("✅ Post created with ID: {}, cache feed cleared", saved.getId());
        return toResponse(saved, userId);
    }

    // ❗ Кэшируем ленту
    @Cacheable(value = "feed", key = "#userId + '_' + #page + '_' + #size")
    public Page<PostResponse> getFeed(Long userId, int page, int size) {
        log.info("📡 Loading feed from DATABASE for user: {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findFeedPosts(userId, pageable);
        return posts.map(post -> toResponse(post, userId));
    }

    // ❗ Кэшируем отдельный пост
    @Cacheable(value = "post", key = "#postId")
    public PostResponse getPostById(Long postId, Long currentUserId) {
        log.info("📡 Loading post from DATABASE: {}", postId);
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            throw new RuntimeException("Post has been deleted");
        }

        return toResponse(post, currentUserId);
    }

    // ❗ При обновлении поста — удаляем из кэша
    @Transactional
    @CacheEvict(value = "post", key = "#postId")
    public PostResponse updatePost(Long postId, Long userId, PostRequest request) {
        log.info("🔄 Updating post: {}, cache will be evicted", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own posts");
        }

        post.setContent(request.content());
        Post updated = postRepository.save(post);

        // Очищаем кэш ленты (так как изменился пост)
        clearFeedCache();

        return toResponse(updated, userId);
    }

    // ❗ При удалении поста — удаляем из кэша
    @Transactional
    @CacheEvict(value = "post", key = "#postId")
    public void deletePost(Long postId, Long userId) {
        log.info("🗑️ Deleting post: {}, cache will be evicted", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        post.setDeleted(true);
        postRepository.save(post);

        // Очищаем кэш ленты
        clearFeedCache();
    }

    // Вспомогательный метод для очистки кэша ленты
    private void clearFeedCache() {
        log.info("🧹 Clearing feed cache");
    }

    private PostResponse toResponse(Post post, Long currentUserId) {
        boolean isLiked = likeRepository.existsByUserIdAndPostIdAndDeletedFalse(currentUserId, post.getId());
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getUser().getUsername(),
                post.getUser().getId(),
                post.getLikesCount(),
                isLiked,
                post.getCreatedAt()
        );
    }
}
