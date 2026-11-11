package com.devhub.devhub.article;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class ParserController {

    private final ArticleParserService parserService;

    public ParserController(ArticleParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/parse/")
    public Map<String , Object> parse(Principal principal) {
        int count = parserService.parseAll(principal.getName());
        String message = count > 0
                ? "Успешно добавлено статей: " + count
                : "Новые статьи не найдены или все уже добавлены";
        return Map.of("ok", true, "message", message, "count", count);
    }
}
