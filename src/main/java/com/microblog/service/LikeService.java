package com.microblog.service;

import com.microblog.entity.Like;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.repository.LikeRepository;
import com.microblog.repository.PostRepository;
import com.microblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // Поставить лайк
    @Transactional
    public String likePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, не поставил ли уже лайк
        if (likeRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, postId)) {
            throw new RuntimeException("You already liked this post");
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);

        likeRepository.save(like);

        // Обновляем счётчик лайков в посте
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);

        return "Post liked successfully";
    }

    // Убрать лайк
    @Transactional
    public String unlikePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Проверяем, что лайк существует
        if (!likeRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, postId)) {
            throw new RuntimeException("You haven't liked this post");
        }

        likeRepository.softDeleteLike(userId, postId);

        // Обновляем счётчик лайков в посте
        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(post);

        return "Post unliked successfully";
    }

    // Проверить, поставил ли пользователь лайк
    public boolean isLiked(Long userId, Long postId) {
        return likeRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, postId);
    }

    // Получить количество лайков у поста
    public long getLikesCount(Long postId) {
        return likeRepository.countByPostIdAndDeletedFalse(postId);
    }
}
