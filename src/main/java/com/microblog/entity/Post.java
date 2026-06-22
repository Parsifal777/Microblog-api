package com.microblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_posts_user_id", columnList = "user_id"),
                @Index(name = "idx_posts_created_at", columnList = "created_at DESC"),
                @Index(name = "idx_posts_user_created", columnList = "user_id, created_at DESC")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 280, message = "Content must be between 1 and 280 characters")
    @Column(nullable = false, length = 280)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;
}
