package com.devhub.devhub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message = "Введите имя пользователя")
                              @Size(max = 150, message = "Слишком длинное имя")
                              String username,

                              @NotBlank(message = "Введите email")
                              @Email(message = "Некорректный email")
                              String email,

                              @NotBlank(message = "Введите пароль")
                              @Size(min = 8, message = "Пароль должен быть не корочк 8 символов")
                              String password,

                              String password2) {}
