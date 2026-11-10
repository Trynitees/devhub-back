package com.devhub.devhub.article;

import com.devhub.devhub.article.dto.ArticleRequest;
import com.devhub.devhub.article.dto.ArticleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/")
    public Map<String, Object> list(@RequestParam(defaultValue = "") String tag,
                                    @RequestParam(defaultValue = "newest") String sort,
                                    Principal principal) {
        return Map.of("ok", true, "articles", articleService.list(tag, sort, username(principal)));
    }

    @GetMapping("/favorites/")
    public Map<String, Object> favorites(Principal principal) {
        return Map.of("ok", true, "articles", articleService.favorites(principal.getName()));
    }

    @GetMapping("/recommendations/")
    public Map<String, Object> recommendations(Principal principal) {
        return Map.of("ok", true, "articles", articleService.recommendation(username(principal)));
    }

    @GetMapping("/{id}/")
    public Map<String, Object> get(@PathVariable Long id, Principal principal) {
        return Map.of("ok", true, "articles", articleService.get(id, username(principal)));
    }

    @PostMapping("/create/")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ArticleRequest req, Principal principal) {
        ArticleResponse article = articleService.create(principal.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true, "article", article));
    }

    @PostMapping("/{id}/edit/")
    public Map<String, Object> edit(@PathVariable Long id,
                                    @Valid @RequestBody ArticleRequest req,
                                    Principal principal) {
        return Map.of("ok", true, "article", articleService.update(id, req, principal.getName()));
    }

    @DeleteMapping("/{id}/delete/")
    public Map<String, Object> delete (@PathVariable Long id) {
        articleService.delete(id);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/favorite")
    public Map<String, Object> favorite(@PathVariable Long id, Principal principal) {
        return articleService.toogleFavorite(id, principal.getName());
    }

    private String username(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
