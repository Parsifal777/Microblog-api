package com.microblog.repository;

import com.microblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. Найти посты пользователя с пагинацией + JOIN FETCH user
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    // 2. Найти все посты пользователя + JOIN FETCH user
    @EntityGraph(attributePaths = {"user"})
    List<Post> findByUserIdAndDeletedFalse(Long userId);

    // 3. Найти пост по ID с JOIN FETCH user
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdWithUser(@Param("id") Long id);

    // 4. Найти пост по ID без JOIN FETCH (для простых операций)
    Optional<Post> findById(Long id);

    // 5. Лента постов с JOIN FETCH user
    @Query("""
        SELECT p FROM Post p 
        JOIN FETCH p.user 
        WHERE p.user.id IN (
            SELECT f.following.id FROM Follow f 
            WHERE f.follower.id = :userId AND f.deleted = false
        ) 
        AND p.deleted = false 
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findFeedPostsWithUser(@Param("userId") Long userId, Pageable pageable);

    // 6. Лента постов без JOIN FETCH (используем EntityGraph)
    @EntityGraph(attributePaths = {"user"})
    @Query("""
        SELECT p FROM Post p 
        WHERE p.user.id IN (
            SELECT f.following.id FROM Follow f 
            WHERE f.follower.id = :userId AND f.deleted = false
        ) 
        AND p.deleted = false 
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findFeedPosts(@Param("userId") Long userId, Pageable pageable);

    // 7. Количество постов пользователя
    long countByUserIdAndDeletedFalse(Long userId);

    // 8. Найти посты с лайками текущего пользователя (для оптимизации)
    @Query("""
        SELECT p FROM Post p 
        JOIN FETCH p.user 
        LEFT JOIN FETCH p.likes l 
        WHERE p.id = :id AND p.deleted = false
    """)
    Optional<Post> findByIdWithUserAndLikes(@Param("id") Long id);
}
