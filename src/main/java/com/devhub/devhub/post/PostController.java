package com.devhub.devhub.post;

import com.devhub.devhub.post.dto.CommentRequest;
import com.devhub.devhub.post.dto.CommentResponse;
import com.devhub.devhub.post.dto.PostResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Principal principal) {
        Page<PostResponse> result = postService.list(username(principal), page, size);
        return Map.of("ok", true,
                "posts", result.getContent(),
                "page", result.getNumber() + 1,
                "size", result.getSize(),
                "total", result.getTotalElements(),
                "total_pages", result.getTotalPages());
    }

    @GetMapping("/{id}/")
    public Map<String, Object> get(@PathVariable Long id, Principal principal) {
        return Map.of("ok", true, "post", postService.get(id, username(principal)));
    }

    @PostMapping(value = "/create/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            Principal principal,
            @RequestParam(name = "title", defaultValue = "") String title,
            @RequestParam(name = "text", defaultValue = "") String text,
            @RequestParam(name = "tags_input", defaultValue = "") String tagsInput,
            @RequestParam(name = "photo", required = false) MultipartFile photo) {

        PostResponse post = postService.create(principal.getName(), title, text, tagsInput, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true, "post", post));
    }

    @DeleteMapping("/{id}/delete/")
    public Map<String, Object> delete(@PathVariable Long id, Principal principal) {
        postService.delete(id, principal.getName());
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/like/")
    public Map<String, Object> like(@PathVariable Long id, Principal principal) {
        return postService.toggleLike(id, principal.getName());
    }

    @GetMapping("/{id}/comments/")
    public Map<String, Object> comments(@PathVariable Long id) {
        return Map.of("ok", true, "comments", postService.comments(id));
    }

    @PostMapping("/{id}/comments/create/")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest req,
            Principal principal) {

        CommentResponse comment = postService.addComment(id, principal.getName(), req.text());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true, "comment", comment));
    }

    private String username(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
