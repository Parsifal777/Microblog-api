package com.microblog.service;

import com.microblog.dto.FollowRequest;
import com.microblog.dto.FollowResponse;
import com.microblog.dto.UserStatsResponse;
import com.microblog.entity.Follow;
import com.microblog.entity.User;
import com.microblog.repository.FollowRepository;
import com.microblog.repository.PostRepository;
import com.microblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    @CacheEvict(value = "userStats", key = "#followingId")  // ❗ Очищаем кэш статистики
    public FollowResponse followUser(Long followerId, FollowRequest request) {
        if (followerId.equals(request.followingId())) {
            throw new RuntimeException("You cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        User following = userRepository.findById(request.followingId())
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        if (followRepository.existsByFollowerIdAndFollowingIdAndDeletedFalse(followerId, request.followingId())) {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);

        log.info("✅ User {} followed {}", followerId, request.followingId());
        return new FollowResponse(
                "Successfully followed user",
                follower.getUsername(),
                following.getUsername()
        );
    }

    @Transactional
    @CacheEvict(value = "userStats", key = "#followingId")
    public FollowResponse unfollowUser(Long followerId, Long followingId) {
        if (!followRepository.existsByFollowerIdAndFollowingIdAndDeletedFalse(followerId, followingId)) {
            throw new RuntimeException("You are not following this user");
        }

        followRepository.softDeleteFollow(followerId, followingId);

        log.info("✅ User {} unfollowed {}", followerId, followingId);
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new FollowResponse(
                "Successfully unfollowed user",
                follower.getUsername(),
                following.getUsername()
        );
    }

    // ❗ Кэшируем статистику
    @Cacheable(value = "userStats", key = "#userId")
    public UserStatsResponse getUserStats(Long userId, Long currentUserId) {
        log.info("📡 Loading user stats from DATABASE for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long postsCount = postRepository.countByUserIdAndDeletedFalse(userId);
        long followersCount = followRepository.countByFollowingIdAndDeletedFalse(userId);
        long followingCount = followRepository.countByFollowerIdAndDeletedFalse(userId);
        boolean isFollowed = followRepository.existsByFollowerIdAndFollowingIdAndDeletedFalse(currentUserId, userId);

        return new UserStatsResponse(
                postsCount,
                followersCount,
                followingCount,
                isFollowed
        );
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingIdAndDeletedFalse(followerId, followingId);
    }
}
