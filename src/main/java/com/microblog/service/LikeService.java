package com.microblog.service;

import com.microblog.entity.Like;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.repository.LikeRepository;
import com.microblog.repository.PostRepository;
import com.microblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LIKES_COUNT_KEY = "post:likes:";
    private static final String USER_LIKES_KEY = "user:likes:";

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

        // Сохраняем в БД
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);

        // ❗ Обновляем счётчик в Redis (атомарно!)
        String likeKey = LIKES_COUNT_KEY + postId;
        Long newCount = redisTemplate.opsForValue().increment(likeKey);
        log.info("❤️ Like count for post {}: {}", postId, newCount);

        // ❗ Сохраняем информацию, что пользователь лайкнул этот пост
        String userLikesKey = USER_LIKES_KEY + userId;
        redisTemplate.opsForSet().add(userLikesKey, postId.toString());

        // Очищаем кэш поста (чтобы обновилось количество лайков)
        redisTemplate.delete("post::" + postId);

        return "Post liked successfully";
    }

    @Transactional
    public String unlikePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!likeRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, postId)) {
            throw new RuntimeException("You haven't liked this post");
        }

        likeRepository.softDeleteLike(userId, postId);

        // ❗ Уменьшаем счётчик в Redis
        String likeKey = LIKES_COUNT_KEY + postId;
        Long newCount = redisTemplate.opsForValue().decrement(likeKey);
        log.info("💔 Like count for post {}: {}", postId, newCount);

        // ❗ Удаляем из множества лайков пользователя
        String userLikesKey = USER_LIKES_KEY + userId;
        redisTemplate.opsForSet().remove(userLikesKey, postId.toString());

        // Очищаем кэш поста
        redisTemplate.delete("post::" + postId);

        return "Post unliked successfully";
    }

    // ❗ Получить количество лайков из Redis (быстро!)
    public long getLikesCount(Long postId) {
        String key = LIKES_COUNT_KEY + postId;
        Object count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            // Если в Redis нет — загружаем из БД и сохраняем в Redis
            long dbCount = likeRepository.countByPostIdAndDeletedFalse(postId);
            redisTemplate.opsForValue().set(key, dbCount, 24, TimeUnit.HOURS);
            log.info("📡 Loaded likes count from DB for post {}: {}", postId, dbCount);
            return dbCount;
        }

        log.info("⚡ Loaded likes count from Redis for post {}: {}", postId, count);
        return ((Number) count).longValue();
    }

    // ❗ Проверить, поставил ли пользователь лайк (из Redis)
    public boolean isLiked(Long userId, Long postId) {
        String key = USER_LIKES_KEY + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, postId.toString());

        if (Boolean.TRUE.equals(isMember)) {
            log.info("⚡ Checked like in Redis for user {} post {}: true", userId, postId);
            return true;
        }

        // Если в Redis нет — проверяем в БД
        boolean dbResult = likeRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, postId);
        if (dbResult) {
            redisTemplate.opsForSet().add(key, postId.toString());
        }
        log.info("📡 Checked like in DB for user {} post {}: {}", userId, postId, dbResult);
        return dbResult;
    }

    // ❗ Синхронизация счётчиков (периодическая задача)
    public void syncLikesCounts() {
        log.info("🔄 Syncing likes counts from DB to Redis");
    }
}
