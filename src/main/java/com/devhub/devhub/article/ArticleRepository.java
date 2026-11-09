package com.devhub.devhub.article;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByTagsName(String tagName, Sort sort);

    List<Article> findByFavoritesUsernameOrderByCreatedDesc(String username);

    boolean existsBySourceUrl(String sourceUrl);

    List<Article> findTop3ByOrderByCreatedDesc();

    @Query("""
    select distinct a from Article a join a.tags t
    where t.id in :tagIds and a.id not in :favoriteIds
    order by a.created desc
    """)

    List<Article> findRecommended(@Param("tagIds")Collection<Long> tagIds,
                                 @Param("favoriteIds") Collection<Long> favoriteIds,
                                 Pageable pageable);

}
