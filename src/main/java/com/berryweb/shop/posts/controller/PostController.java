package com.berryweb.shop.posts.controller;

import com.berryweb.shop.posts.dto.PostCreateReq;
import com.berryweb.shop.posts.dto.PostUpdateReq;
import com.berryweb.shop.posts.entity.Post;
import com.berryweb.shop.posts.service.CommentService;
import com.berryweb.shop.posts.service.PostFileService;
import com.berryweb.shop.posts.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostFileService postFileService;

    // 게시글 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", post);
        response.put("comments", commentService.getCommentsByPostId(id));
        response.put("files", postFileService.getFilesByPostId(id));
        response.put("commentCount", commentService.getCommentCountByPostId(id));
        response.put("fileCount", postFileService.getFileCountByPostId(id));

        return ResponseEntity.ok(response);
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostCreateReq request) {
        System.out.println("Received post data: " + request); // 디버깅용

        try {
            Post post = postService.createPost(
                    request.getTitle(),
                    request.getContent(),
                    request.getAuthor()
            );
            System.out.println("Created post: " + post); // 디버깅용
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            System.err.println("Error creating post: " + e.getMessage()); // 에러 로깅
            return ResponseEntity.badRequest().build();
        }
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateReq request) {

        Post post = postService.updatePost(id, request.getTitle(), request.getContent());
        return ResponseEntity.ok(post);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (title != null && !title.trim().isEmpty()) {
            posts = postService.searchByTitle(title, pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            posts = postService.searchByAuthor(author, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.searchByTitleOrContent(keyword, pageable);
        } else {
            posts = postService.getAllPosts(pageable);
        }

        return ResponseEntity.ok(posts);
    }

    // 파일이 첨부된 게시글 조회
    @GetMapping("/with-files")
    public ResponseEntity<Page<Post>> getPostsWithFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getPostsWithFiles(pageable);
        return ResponseEntity.ok(posts);
    }

    // 게시판 통계
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postService.getTotalPostCount());
        stats.put("totalFileSize", postFileService.formatFileSize(postFileService.getTotalFileSize()));

        return ResponseEntity.ok(stats);
    }

}
