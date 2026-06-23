package com.microblog.repository;

import com.microblog.entity.Follow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 1. Проверить, подписан ли пользователь
    boolean existsByFollowerIdAndFollowingIdAndDeletedFalse(Long followerId, Long followingId);

    // 2. Найти подписку с JOIN FETCH пользователей
    @EntityGraph(attributePaths = {"follower", "following"})
    Optional<Follow> findByFollowerIdAndFollowingIdAndDeletedFalse(Long followerId, Long followingId);

    // 3. Количество подписчиков
    long countByFollowingIdAndDeletedFalse(Long userId);

    // 4. Количество подписок
    long countByFollowerIdAndDeletedFalse(Long userId);

    // 5. Мягкое удаление подписки
    @Modifying
    @Query("UPDATE Follow f SET f.deleted = true WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    void softDeleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // 6. Найти всех подписчиков пользователя с JOIN FETCH
    @EntityGraph(attributePaths = {"follower"})
    List<Follow> findByFollowingIdAndDeletedFalse(Long userId);

    // 7. Найти всех, на кого подписан пользователь с JOIN FETCH
    @EntityGraph(attributePaths = {"following"})
    List<Follow> findByFollowerIdAndDeletedFalse(Long userId);

    // 8. Проверить подписку с JOIN FETCH
    @Query("""
        SELECT f FROM Follow f 
        JOIN FETCH f.follower 
        JOIN FETCH f.following 
        WHERE f.follower.id = :followerId 
        AND f.following.id = :followingId 
        AND f.deleted = false
    """)
    Optional<Follow> findFollowWithUsers(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
