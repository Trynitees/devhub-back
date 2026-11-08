package com.devhub.devhub.post.dto;

import com.devhub.devhub.post.Post;
import com.devhub.devhub.post.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(Long id, String title, String text, String photo, AuthorResponse author,
                           List<String> tags, LocalDateTime created, int likesCount, long commentsCount,
                           @JsonProperty("is_liked") boolean isLiked) {
    public static PostResponse from(Post post, long commentsCount, boolean liked){
        List<String> tagNames = post.getTags().stream().map(Tag::getName).sorted().toList();
        return new PostResponse(post.getId(), post.getTitle(), post.getText(), post.getPhoto(),
                AuthorResponse.from(post.getAuthor()), tagNames, post.getCreated(),
                post.getLikes().size(), commentsCount, liked);
    }

}
