package com.devhub.devhub.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedDesc();

    List<Post> findByStatusOrderByCreatedAsc(PostStatus status);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);
}
