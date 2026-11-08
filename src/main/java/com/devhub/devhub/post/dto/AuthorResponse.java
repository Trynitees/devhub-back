package com.devhub.devhub.post.dto;

import com.devhub.devhub.user.User;

public record AuthorResponse(Long id, String name, String avatar) {

    public static AuthorResponse from(User u){
        return new AuthorResponse(u.getId(), u.getUsername(), u.getAvatar());
    }
}
