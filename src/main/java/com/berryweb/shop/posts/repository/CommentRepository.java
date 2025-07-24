package com.berryweb.shop.posts.repository;

import com.berryweb.shop.posts.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 조회 (최신순)
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    // 특정 게시글의 댓글 조회 (오래된순)
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 특정 작성자의 댓글 조회
    Page<Comment> findByAuthorOrderByCreatedAtDesc(String author, Pageable pageable);

    // 특정 게시글의 댓글 개수
    long countByPostId(Long postId);

    // 특정 작성자의 댓글 개수
    long countByAuthor(String author);

    // 댓글 내용으로 검색
    Page<Comment> findByContentContainingIgnoreCase(String content, Pageable pageable);

    // 특정 게시글에서 특정 작성자의 댓글
    List<Comment> findByPostIdAndAuthor(Long postId, String author);

    // 게시글별 댓글 개수 조회 (통계용)
    @Query("SELECT c.post.id, COUNT(c) FROM Comment c GROUP BY c.post.id")
    List<Object[]> countCommentsByPost();

}
