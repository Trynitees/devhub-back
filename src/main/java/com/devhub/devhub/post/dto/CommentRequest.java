package com.devhub.devhub.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(@NotBlank(message = "Введите комментарий")
                             @Size(max = 1000, message = "Не более 1000 символов")
                             String text) {}
