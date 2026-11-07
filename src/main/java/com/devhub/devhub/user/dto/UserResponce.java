package com.devhub.devhub.user.dto;

import com.devhub.devhub.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponce(
        Long id,
        String username,
        String email,
        String firstname,
        String lastname,
        String patronomic,
        String bio,
        String avatar,
        @JsonProperty("is_staff") boolean isStaff) {
    public static UserResponce from(User u){
        return new UserResponce(u.getId(), u.getUsername(), u.getEmail(), u.getFirstname(), u.getLastname(),u.getPatronomic(), u.getBio(), u.getAvatar(), u.isStaff());

    }
}
