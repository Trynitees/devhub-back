package com.devhub.devhub.article.dto;

import com.devhub.devhub.article.Article;
import com.devhub.devhub.post.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record ArticleResponse(
        Long id,
        String title,
        String text,
        String sourceUrl,
        List<String> tags,
        String author,
        String created,
        @JsonProperty("is_favorited") boolean isFavorited
) {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static ArticleResponse from(Article a, boolean favorited){
        List<String> tagNames = a.getTags().stream().map(Tag::getName).sorted().toList();
        String author = a.getAuthor() != null ? a.getAuthor().getUsername() : "Админ";
        return new ArticleResponse(a.getId(), a.getTitle(), a.getText(), a.getSourceUrl(),
                tagNames, author, a.getCreated().format(DATE), favorited);
    }
}
