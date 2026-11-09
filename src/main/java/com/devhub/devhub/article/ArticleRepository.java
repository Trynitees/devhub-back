package com.devhub.devhub.article;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByTagsName(String tagName, Sort sort);
    List<Article> findByFavoriteUsernameOrderByCreatedDesc(String username);

    boolean existsBySourceUrl(String sourceUrl);
}
