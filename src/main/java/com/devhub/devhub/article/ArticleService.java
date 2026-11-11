package com.devhub.devhub.article;

import com.devhub.devhub.article.dto.ArticleRequest;
import com.devhub.devhub.article.dto.ArticleResponse;
import com.devhub.devhub.common.ApiException;
import com.devhub.devhub.common.HtmlCleaner;
import com.devhub.devhub.post.Tag;
import com.devhub.devhub.post.TagRepository;
import com.devhub.devhub.user.User;
import com.devhub.devhub.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final HtmlCleaner htmlCleaner;

    public ArticleService(ArticleRepository articleRepository,
                          TagRepository tagRepository,
                          UserService userService,
                          HtmlCleaner htmlCleaner) {
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
        this.htmlCleaner = htmlCleaner;
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> list(String tag, String sort, String username, int page, int size) {
        Sort order = switch (sort) {
            case "oldest" -> Sort.by("created").ascending();
            case "title" -> Sort.by("title").ascending();
            default -> Sort.by("created").descending();
        };
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1,
                Math.min(Math.max(size, 1), 50),
                order);
        Page<Article> result = tag.isBlank()
                ? articleRepository.findAll(pageable)
                : articleRepository.findByTagsName(tag, pageable);
        return result.map(a -> toResponse(a, username));
    }

    @Transactional(readOnly = true)
    public ArticleResponse get(Long id, String username){
        return toResponse(find(id), username);
    }

    @Transactional
    public ArticleResponse create(String username, ArticleRequest req) {
        Article article = new Article();
        article.setAuthor(userService.getByUsername(username));
        fill(article, req);
        articleRepository.save(article);
        return toResponse(article, username);
    }

    @Transactional
    public ArticleResponse update(Long id, ArticleRequest req, String username) {
        Article article = find(id);
        fill(article, req);
        articleRepository.save(article);
        return toResponse(article, username);
    }

    @Transactional
    public void delete(Long id) {
        articleRepository.delete(find(id));
    }

    @Transactional
    public Map<String, Object> toogleFavorite(Long id, String username) {
        Article article = find(id);
        User user = userService.getByUsername(username);

        boolean removed = article.getFavorites().removeIf(u -> u.getId().equals(user.getId()));
        if (!removed) {
            article.getFavorites().add(user);
        }
        articleRepository.save(article);

        return Map.of("ok", true, "is_favorited", !removed);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> favorites(String username) {
        return articleRepository.findByFavoritesUsernameOrderByCreatedDesc(username).stream()
                .map(a -> toResponse(a, username)).toList();
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> recommendation(String username) {
        if (username == null){
            return List.of();
        }
        List<Article> favorites = articleRepository.findByFavoritesUsernameOrderByCreatedDesc(username);

        Set<Long> tagIds = favorites.stream()
                .flatMap(a -> a.getTags().stream())
                .map(Tag::getId)
                .collect(Collectors.toSet());

        List<Article> result;
        if (tagIds.isEmpty()) {
            result = articleRepository.findTop3ByOrderByCreatedDesc();
        } else {
            List<Long> favoriteIds = favorites.stream().map(Article::getId).toList();
            result = articleRepository.findRecommended(tagIds, favoriteIds, PageRequest.of(0,3));
        }
        return result.stream().map(a -> toResponse(a, username)).toList();
    }

    private Article find(Long id){
        return articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "detail", "Статья не обнаружена)"));
    }

    private void fill(Article article, ArticleRequest req) {
        String text = htmlCleaner.clean(req.text().trim());
        if (text.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "text",
                    "Текст статьи пуст или содержит только запрещенные элементы");
        }
        article.setTitle(req.title().trim());
        article.setText(text);

        String url = req.sourceUrl() == null ? "" : req.sourceUrl().trim();
        article.setSourceUrl(url.isEmpty() ? null : url);

        article.getTags().clear();
        String tagsInput = req.tagsInput() == null ? "" : req.tagsInput();
        for (String name : tagsInput.split(",")) {
            String tagName = name.trim();
            if (!tagName.isEmpty()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                article.getTags().add(tag);
            }
        }
    }

    private ArticleResponse toResponse(Article article, String username) {
        boolean favorited = username != null
                && article.getFavorites().stream().anyMatch(u -> u.getUsername().equals(username));
        return ArticleResponse.from(article, favorited);
    }

}
