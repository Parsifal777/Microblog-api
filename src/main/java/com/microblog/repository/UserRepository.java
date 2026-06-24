package com.microblog.repository;

import com.microblog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Базовые методы (работают автоматически)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // JOIN FETCH через явный @Query
    @Query("SELECT u FROM User u JOIN FETCH u.posts WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdWithPosts(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.followers LEFT JOIN FETCH u.following WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdWithFollows(@Param("id") Long id);
}
