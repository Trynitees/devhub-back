package com.devhub.devhub.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAsc(Long postId);
    long countByPostId(Long postId);
}
