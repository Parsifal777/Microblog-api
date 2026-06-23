package com.microblog.repository;

import com.microblog.entity.Like;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // 1. Проверить, поставил ли пользователь лайк
    boolean existsByUserIdAndPostIdAndDeletedFalse(Long userId, Long postId);

    // 2. Найти лайк с JOIN FETCH user и post
    @EntityGraph(attributePaths = {"user", "post"})
    Optional<Like> findByUserIdAndPostIdAndDeletedFalse(Long userId, Long postId);

    // 3. Количество лайков у поста
    long countByPostIdAndDeletedFalse(Long postId);

    // 4. Мягкое удаление лайка
    @Modifying
    @Query("UPDATE Like l SET l.deleted = true WHERE l.user.id = :userId AND l.post.id = :postId")
    void softDeleteLike(@Param("userId") Long userId, @Param("postId") Long postId);

    // 5. Найти все лайки поста с JOIN FETCH пользователей
    @EntityGraph(attributePaths = {"user"})
    List<Like> findByPostIdAndDeletedFalse(Long postId);

    // 6. Найти все лайки пользователя с JOIN FETCH постов
    @EntityGraph(attributePaths = {"post"})
    List<Like> findByUserIdAndDeletedFalse(Long userId);

    // 7. Проверить лайк с JOIN FETCH
    @Query("""
        SELECT l FROM Like l 
        JOIN FETCH l.user 
        JOIN FETCH l.post 
        WHERE l.user.id = :userId 
        AND l.post.id = :postId 
        AND l.deleted = false
    """)
    Optional<Like> findLikeWithUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);
}
