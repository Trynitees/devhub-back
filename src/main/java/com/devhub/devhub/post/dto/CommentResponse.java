package com.devhub.devhub.post.dto;

import com.devhub.devhub.post.Comment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String text, AuthorResponse author, LocalDateTime createdAt) {

    public static CommentResponse from(Comment c){
        return new CommentResponse(c.getId(), c.getText(), AuthorResponse.from(c.getAuthor()), c.getCreated());
    }
}
