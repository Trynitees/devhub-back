package com.devhub.devhub.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ArticleRequest(
        @NotBlank(message = "Введите заголовок")
        @Size( max = 200, message = "Не более 200 символов")
        String title,

        @NotBlank(message = "Введите текст статьи")
        String text,

        @URL(message = "Введите корректную ссылку")
        @Size(max = 1000, message = "Слишком длинная ссылка")
        String sourceUrl,

        String tagsInput
) {}
