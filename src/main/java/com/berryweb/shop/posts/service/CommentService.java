package com.berryweb.shop.posts.service;

import com.berryweb.shop.posts.entity.Comment;
import com.berryweb.shop.posts.entity.Post;
import com.berryweb.shop.posts.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    // 특정 게시글의 댓글 조회 (최신순)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    // 특정 게시글의 댓글 조회 (오래된순)
    public List<Comment> getCommentsByPostIdAsc(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    // 댓글 상세 조회
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + id));
    }

    // 댓글 생성
    @Transactional
    public Comment createComment(Long postId, String content, String author) {
        Post post = postService.getPostById(postId);
        Comment comment = new Comment(content, author, post);
        return commentRepository.save(comment);
    }

    // 댓글 수정
    @Transactional
    public Comment updateComment(Long id, String content) {
        Comment comment = getCommentById(id);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        commentRepository.delete(comment);
    }

    // 작성자별 댓글 조회
    public Page<Comment> getCommentsByAuthor(String author, Pageable pageable) {
        return commentRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    // 댓글 내용으로 검색
    public Page<Comment> searchCommentsByContent(String content, Pageable pageable) {
        return commentRepository.findByContentContainingIgnoreCase(content, pageable);
    }

    // 특정 게시글의 댓글 개수
    public long getCommentCountByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    // 특정 작성자의 댓글 개수
    public long getCommentCountByAuthor(String author) {
        return commentRepository.countByAuthor(author);
    }

    // 특정 게시글에서 특정 작성자의 댓글
    public List<Comment> getCommentsByPostIdAndAuthor(Long postId, String author) {
        return commentRepository.findByPostIdAndAuthor(postId, author);
    }

    // 게시글별 댓글 개수 통계
    public List<Object[]> getCommentCountsByPost() {
        return commentRepository.countCommentsByPost();
    }

}
