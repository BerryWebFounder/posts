package com.berryweb.shop.posts.repository;

import com.berryweb.shop.posts.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 제목으로 검색 (페이징)
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 작성자로 검색 (페이징)
    Page<Post> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // 제목 또는 내용으로 검색 (페이징)
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    // 최신 게시글 조회 (페이징)
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 작성자의 게시글 개수
    long countByAuthor(String author);

    // 댓글이 있는 게시글만 조회
    @Query("SELECT DISTINCT p FROM Post p JOIN p.comments")
    List<Post> findPostsWithComments();

    // 파일이 첨부된 게시글만 조회
    @Query("SELECT DISTINCT p FROM Post p JOIN p.files")
    Page<Post> findPostsWithFiles(Pageable pageable);

}
