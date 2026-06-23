package com.microblog.repository;

import com.microblog.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Найти по username
    Optional<User> findByUsername(String username);

    // 2. Найти по username с JOIN FETCH постов (для оптимизации)
    @EntityGraph(attributePaths = {"posts"})
    Optional<User> findByUsernameWithPosts(String username);

    // 3. Найти по email
    Optional<User> findByEmail(String email);

    // 4. Проверить существование
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 5. Найти пользователя с его подписками (JOIN FETCH)
    @EntityGraph(attributePaths = {"followers", "following"})
    Optional<User> findByIdWithFollows(Long id);

    // 6. Найти пользователя с его постами (JOIN FETCH)
    @EntityGraph(attributePaths = {"posts"})
    Optional<User> findByIdWithPosts(Long id);

    // 7. Найти пользователя с полной информацией (JOIN FETCH)
    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.posts 
        LEFT JOIN FETCH u.followers 
        LEFT JOIN FETCH u.following 
        WHERE u.id = :id AND u.deleted = false
    """)
    Optional<User> findByIdWithAllData(@Param("id") Long id);
}
