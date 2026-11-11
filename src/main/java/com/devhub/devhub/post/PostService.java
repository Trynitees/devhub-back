package com.devhub.devhub.post;

import com.devhub.devhub.common.ApiException;
import com.devhub.devhub.common.FileStorageService;
import com.devhub.devhub.post.dto.CommentResponse;
import com.devhub.devhub.post.dto.PostResponse;
import com.devhub.devhub.user.User;
import com.devhub.devhub.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private  final FileStorageService fileStorageService;

    public PostService(PostRepository postRepository,
                       TagRepository tagRepository,
                       CommentRepository commentRepository,
                       UserService userService,
                       FileStorageService fileStorageService){
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> list(String username, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1,
                Math.min(Math.max(size, 1), 50),
                Sort.by("created").descending());
        return postRepository.findAll(pageable).map(post -> toResponse(post, username));
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id, String username){
        return toResponse(findPost(id), username);
    }

    @Transactional
    public PostResponse create(String username, String title, String text, String tagsInput, MultipartFile photo){
        String cleanTitle = title.trim();
        String cleanText = text.trim();
        if (cleanTitle.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "title", "Введите заголовок");
        }
        if (cleanTitle.length() > 128) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "title", "Не более 128 символов");
        }
        if (cleanText.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "text", "Введите текст");
        }
        if (cleanText.length() > 5000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "text", "Не более 5000 символов");
        }

        Post post = new Post();
        post.setTitle(cleanTitle);
        post.setText(cleanText);
        post.setAuthor(userService.getByUsername(username));

        if (photo != null && !photo.isEmpty()) {
            post.setPhoto(fileStorageService.save(photo, "posts"));
        }

        for (String name : tagsInput.split(",")) {
            String tagName = name.trim();
            if (!tagName.isEmpty()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                post.getTags().add(tag);
            }
        }

        postRepository.save(post);
        return toResponse(post, username);
    }

    @Transactional
    public void delete(Long id, String username) {
        Post post = findPost(id);
        User user = userService.getByUsername(username);
        if (!post.getAuthor().getId().equals(user.getId()) && !user.isStaff()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "detail",
                    "Только автор или админ может удалить пост");
        }
        postRepository.delete(post);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long id, String username) {
        Post post = findPost(id);
        User user = userService.getByUsername(username);

        boolean removed = post.getLikes().removeIf(u -> u.getId().equals(user.getId()));
        if (!removed) {
            post.getLikes().add(user);
        }
        postRepository.save(post);

        return Map.of("ok", true, "is_liked", !removed, "likes_count", post.getLikes().size());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> comments(Long postId) {
        findPost(postId);
        return commentRepository.findByPostIdOrderByCreatedAsc(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long postId, String username, String text) {
        Post post = findPost(postId);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(userService.getByUsername(username));
        comment.setText(text.trim());
        return CommentResponse.from(commentRepository.save(comment));
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "detail", "Пост не найден"));
    }

    private PostResponse toResponse(Post post, String username) {
        boolean liked = username != null
                && post.getLikes().stream().anyMatch(u -> u.getUsername().equals(username));
        long commentsCount = commentRepository.countByPostId(post.getId());
        return PostResponse.from(post, commentsCount, liked);
    }
}
