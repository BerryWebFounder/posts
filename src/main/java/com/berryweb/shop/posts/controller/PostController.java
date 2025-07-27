package com.berryweb.shop.posts.controller;

import com.berryweb.shop.posts.dto.PostCreateReq;
import com.berryweb.shop.posts.dto.PostUpdateReq;
import com.berryweb.shop.posts.entity.Comment;
import com.berryweb.shop.posts.entity.Post;
import com.berryweb.shop.posts.entity.PostFile;
import com.berryweb.shop.posts.service.CommentService;
import com.berryweb.shop.posts.service.PostFileService;
import com.berryweb.shop.posts.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostFileService postFileService;

    // ============ 기존 게시글 API ============

    // 일반 게시글 목록 조회 (페이징) - 공지사항 제외
    @GetMapping("/posts")
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // 전체 게시글 목록 조회 (공지사항 + 일반 게시글)
    @GetMapping("/posts/all")
    public ResponseEntity<Page<Post>> getAllPostsWithNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getAllPostsWithNotices(pageable);
        return ResponseEntity.ok(posts);
    }

    // 게시글/공지사항 상세 조회
    @GetMapping("/posts/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long id) {
        System.out.println("=== 게시글 상세 조회 시작 ===");
        System.out.println("요청된 게시글 ID: " + id);

        try {
            // 게시글 조회 (조회수 증가)
            Post post = postService.getPostByIdWithViewCount(id);
            System.out.println("게시글 조회 완료: " + post.getTitle());

            // 댓글 조회
            List<Comment> comments = commentService.getCommentsByPostId(id);
            System.out.println("댓글 조회 완료: " + comments.size() + "개");

            // 파일 조회
            List<PostFile> files = postFileService.getFilesByPostId(id);
            System.out.println("파일 조회 완료: " + files.size() + "개");

            // 통계 정보
            long commentCount = commentService.getCommentCountByPostId(id);
            long fileCount = postFileService.getFileCountByPostId(id);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("post", post);  // "posts" → "post"로 변경 (더 명확함)
            response.put("comments", comments);
            response.put("files", files);
            response.put("commentCount", commentCount);
            response.put("fileCount", fileCount);

            // 게시글 타입 정보 추가
            response.put("isNotice", post.isNotice());
            response.put("isPinned", post.isPinned());
            response.put("isActive", post.isActive());
            response.put("hasFiles", !files.isEmpty());
            response.put("hasComments", !comments.isEmpty());

            // 파일 정보 요약
            if (!files.isEmpty()) {
                List<Map<String, Object>> filesSummary = files.stream().map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("id", file.getId());
                    fileInfo.put("originalName", file.getOriginalName());
                    fileInfo.put("storedName", file.getStoredName());
                    fileInfo.put("fileSize", file.getFileSize());
                    fileInfo.put("formattedFileSize", file.getFormattedFileSize());
                    fileInfo.put("contentType", file.getContentType());
                    fileInfo.put("isImage", file.isImage());
                    fileInfo.put("downloadUrl", file.getDownloadUrl());
                    fileInfo.put("postId", file.getPostId());
                    fileInfo.put("createdAt", file.getCreatedAt());
                    return fileInfo;
                }).toList();

                response.put("filesDetail", filesSummary);
            }

            System.out.println("=== 게시글 상세 조회 완료 ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("게시글 상세 조회 실패: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글을 불러올 수 없습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 게시글 생성 (일반 게시글 또는 공지사항)
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody PostCreateReq request) {
        System.out.println("Received post data: " + request);

        try {
            Post post;

            // 공지사항인지 확인
            if (Boolean.TRUE.equals(request.getIsNotice())) {
                post = postService.createNotice(
                        request.getTitle(),
                        request.getContent(),
                        request.getAuthor(),
                        request.getIsPinned(),
                        request.getIsActive(),
                        request.getExpiryDate(),
                        request.getSendNotification()
                );
            } else {
                post = postService.createPost(
                        request.getTitle(),
                        request.getContent(),
                        request.getAuthor()
                );
            }

            System.out.println("Created post: " + post);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            System.err.println("Error creating post: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 게시글 수정 (일반 게시글 또는 공지사항)
    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateReq request) {

        try {
            Post existingPost = postService.getPostById(id);
            Post updatedPost;

            if (existingPost.isNotice()) {
                // 공지사항 수정
                updatedPost = postService.updateNotice(
                        id,
                        request.getTitle(),
                        request.getContent(),
                        request.getIsPinned(),
                        request.getIsActive(),
                        request.getExpiryDate(),
                        request.getSendNotification()
                );
            } else {
                // 일반 게시글 수정
                updatedPost = postService.updatePost(id, request.getTitle(), request.getContent());
            }

            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            System.err.println("Error updating post: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 게시글 검색 (일반 게시글)
    @GetMapping("/posts/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (title != null && !title.trim().isEmpty()) {
            posts = postService.searchByTitle(title, pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            posts = postService.searchByAuthor(author, pageable);
        } else if (content != null && !content.trim().isEmpty()) {
            // 내용 검색은 키워드 검색으로 처리
            posts = postService.searchByTitleOrContent(content, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.searchByTitleOrContent(keyword, pageable);
        } else {
            posts = postService.getAllPosts(pageable);
        }

        return ResponseEntity.ok(posts);
    }

    // 파일이 첨부된 게시글 조회
    @GetMapping("/posts/with-files")
    public ResponseEntity<Page<Post>> getPostsWithFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getPostsWithFiles(pageable);
        return ResponseEntity.ok(posts);
    }

    // ============ 공지사항 전용 API ============

    // 전체 공지사항 조회 (관리자용)
    @GetMapping("/notices")
    public ResponseEntity<Page<Post>> getAllNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> notices = postService.getAllNotices(pageable);
        return ResponseEntity.ok(notices);
    }

    // 활성 공지사항만 조회
    @GetMapping("/notices/active")
    public ResponseEntity<List<Post>> getActiveNotices() {
        List<Post> notices = postService.getActiveNoticesList();
        return ResponseEntity.ok(notices);
    }

    // 공지사항 상세 조회 (별도 엔드포인트)
    @GetMapping("/notices/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getNoticeById(@PathVariable Long id) {
        System.out.println("=== 공지사항 상세 조회 시작 ===");
        System.out.println("요청된 공지사항 ID: " + id);

        try {
            Post notice = postService.getPostByIdWithViewCount(id);

            if (!notice.isNotice()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "요청한 ID는 공지사항이 아닙니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<Comment> comments = commentService.getCommentsByPostId(id);
            List<PostFile> files = postFileService.getFilesByPostId(id);
            long commentCount = commentService.getCommentCountByPostId(id);
            long fileCount = postFileService.getFileCountByPostId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("notice", notice);  // "post" 대신 "notice" 사용
            response.put("comments", comments);
            response.put("files", files);
            response.put("commentCount", commentCount);
            response.put("fileCount", fileCount);

            // 공지사항 전용 정보
            response.put("isPinned", notice.isPinned());
            response.put("isActive", notice.isActive());
            response.put("isExpired", notice.isExpired());
            response.put("expiryDate", notice.getExpiryDate());
            response.put("hasFiles", !files.isEmpty());
            response.put("hasComments", !comments.isEmpty());

            // 파일 정보 요약 (게시글과 동일)
            if (!files.isEmpty()) {
                List<Map<String, Object>> filesSummary = files.stream().map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("id", file.getId());
                    fileInfo.put("originalName", file.getOriginalName());
                    fileInfo.put("storedName", file.getStoredName());
                    fileInfo.put("fileSize", file.getFileSize());
                    fileInfo.put("formattedFileSize", file.getFormattedFileSize());
                    fileInfo.put("contentType", file.getContentType());
                    fileInfo.put("isImage", file.isImage());
                    fileInfo.put("downloadUrl", file.getDownloadUrl());
                    fileInfo.put("postId", file.getPostId());
                    fileInfo.put("createdAt", file.getCreatedAt());
                    return fileInfo;
                }).toList();

                response.put("filesDetail", filesSummary);
            }

            System.out.println("=== 공지사항 상세 조회 완료 ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("공지사항 상세 조회 실패: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "공지사항을 불러올 수 없습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 공지사항 생성 (전용 엔드포인트)
    @PostMapping("/notices")
    public ResponseEntity<Post> createNotice(@RequestBody PostCreateReq request) {
        System.out.println("Creating notice: " + request);

        try {
            Post notice = postService.createNotice(
                    request.getTitle(),
                    request.getContent(),
                    request.getAuthor(),
                    request.getIsPinned(),
                    request.getIsActive(),
                    request.getExpiryDate(),
                    request.getSendNotification()
            );

            System.out.println("Created notice: " + notice);
            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            System.err.println("Error creating notice: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 수정 (전용 엔드포인트)
    @PutMapping("/notices/{id}")
    public ResponseEntity<Post> updateNotice(
            @PathVariable Long id,
            @RequestBody PostUpdateReq request) {

        try {
            Post notice = postService.updateNotice(
                    id,
                    request.getTitle(),
                    request.getContent(),
                    request.getIsPinned(),
                    request.getIsActive(),
                    request.getExpiryDate(),
                    request.getSendNotification()
            );

            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            System.err.println("Error updating notice: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 삭제 (전용 엔드포인트)
    @DeleteMapping("/notices/{id}")
    public ResponseEntity<Map<String, String>> deleteNotice(@PathVariable Long id) {
        Post notice = postService.getPostById(id);

        if (!notice.isNotice()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "요청한 ID는 공지사항이 아닙니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        postService.deletePost(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "공지사항이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 공지사항 상태 토글
    @PatchMapping("/notices/{id}/toggle-status")
    public ResponseEntity<Post> toggleNoticeStatus(@PathVariable Long id) {
        try {
            Post notice = postService.toggleNoticeStatus(id);
            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            System.err.println("Error toggling notice status: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 검색
    @GetMapping("/notices/search")
    public ResponseEntity<Page<Post>> searchNotices(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> notices;

        if (title != null && !title.trim().isEmpty()) {
            notices = postService.searchNoticesByTitle(title, pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            notices = postService.searchNoticesByAuthor(author, pageable);
        } else if (content != null && !content.trim().isEmpty()) {
            notices = postService.searchNoticesByContent(content, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            notices = postService.searchNoticesByKeyword(keyword, pageable);
        } else {
            notices = postService.getAllNotices(pageable);
        }

        return ResponseEntity.ok(notices);
    }

    // 중요 공지사항 조회
    @GetMapping("/notices/pinned")
    public ResponseEntity<List<Post>> getPinnedNotices() {
        List<Post> notices = postService.getPinnedNotices();
        return ResponseEntity.ok(notices);
    }

    // 일반 공지사항 조회 (중요공지 제외)
    @GetMapping("/notices/regular")
    public ResponseEntity<List<Post>> getRegularNotices() {
        List<Post> notices = postService.getRegularNotices();
        return ResponseEntity.ok(notices);
    }

    // 만료 임박 공지사항 조회
    @GetMapping("/notices/expiring-soon")
    public ResponseEntity<List<Post>> getNoticesExpiringSoon() {
        List<Post> notices = postService.getNoticesExpiringSoon();
        return ResponseEntity.ok(notices);
    }

    // ============ 통계 API ============

    // 전체 통계
    @GetMapping("/posts/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 게시글 통계
        stats.put("totalPosts", postService.getTotalPostCount());
        stats.put("regularPosts", postService.getRegularPostCount());
        stats.put("totalNotices", postService.getNoticeCount());
        stats.put("activeNotices", postService.getActiveNoticeCount());
        stats.put("pinnedNotices", postService.getPinnedNoticeCount());
        stats.put("expiredNotices", postService.getExpiredNoticeCount());

        // 파일 통계
        stats.put("totalFileSize", postFileService.formatFileSize(postFileService.getTotalFileSize()));

        return ResponseEntity.ok(stats);
    }

    // 공지사항 통계
    @GetMapping("/notices/stats")
    public ResponseEntity<Map<String, Object>> getNoticeStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", postService.getNoticeCount());
        stats.put("active", postService.getActiveNoticeCount());
        stats.put("pinned", postService.getPinnedNoticeCount());
        stats.put("expired", postService.getExpiredNoticeCount());
        stats.put("expiringSoon", postService.getNoticesExpiringSoon().size());

        return ResponseEntity.ok(stats);
    }

}