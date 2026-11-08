package com.devhub.devhub.user.dto;

import com.devhub.devhub.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String patronymic,
        String bio,
        String avatar,
        @JsonProperty("is_staff") boolean isStaff) {
    public static UserResponse from(User u){
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getFirstname(), u.getLastname(),u.getPatronymic(), u.getBio(), u.getAvatar(), u.isStaff());

    }
}
