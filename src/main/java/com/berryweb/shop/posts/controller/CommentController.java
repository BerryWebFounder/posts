package com.berryweb.shop.posts.controller;

import com.berryweb.shop.posts.dto.CommentCreateReq;
import com.berryweb.shop.posts.dto.CommentUpdateReq;
import com.berryweb.shop.posts.entity.Comment;
import com.berryweb.shop.posts.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    // 특정 게시글의 댓글 조회 (일반 게시글 + 공지사항 모두 지원)
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    // 특정 공지사항의 댓글 조회 (별도 엔드포인트, 동일한 로직)
    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<List<Comment>> getCommentsByNoticeId(@PathVariable Long noticeId) {
        List<Comment> comments = commentService.getCommentsByPostId(noticeId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    // 댓글 생성 (일반 게시글 + 공지사항 모두 지원)
    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody CommentCreateReq request) {
        Comment comment = commentService.createComment(
                request.getPostId(),
                request.getContent(),
                request.getAuthor()
        );
        return ResponseEntity.ok(comment);
    }

    // 댓글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @RequestBody CommentUpdateReq request) {

        Comment comment = commentService.updateComment(id, request.getContent());
        return ResponseEntity.ok(comment);
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 작성자별 댓글 조회
    @GetMapping("/author/{author}")
    public ResponseEntity<Page<Comment>> getCommentsByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getCommentsByAuthor(author, pageable);
        return ResponseEntity.ok(comments);
    }

    // 댓글 검색
    @GetMapping("/search")
    public ResponseEntity<Page<Comment>> searchComments(
            @RequestParam String content,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.searchCommentsByContent(content, pageable);
        return ResponseEntity.ok(comments);
    }
}