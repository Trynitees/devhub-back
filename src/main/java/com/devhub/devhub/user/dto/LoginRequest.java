package com.devhub.devhub.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Введите имя пользователя") String username,
        @NotBlank(message = "Введите пароль") String password) {}
