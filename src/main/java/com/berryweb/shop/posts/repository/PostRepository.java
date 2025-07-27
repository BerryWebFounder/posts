package com.berryweb.shop.posts.repository;

import com.berryweb.shop.posts.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ============ 기존 쿼리들 ============

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

    // ============ 공지사항 관련 쿼리들 ============

    // 일반 게시글만 조회 (공지사항 제외)
    Page<Post> findByIsNoticeFalseOrderByCreatedAtDesc(Pageable pageable);

    // 공지사항만 조회 (전체)
    Page<Post> findByIsNoticeTrueOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    // 활성 공지사항만 조회
    Page<Post> findByIsNoticeTrueAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    // 활성 공지사항만 조회 (List 형태) - 메인 페이지용
    List<Post> findByIsNoticeTrueAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc();

    // 중요 공지사항만 조회
    List<Post> findByIsNoticeTrueAndIsPinnedTrueAndIsActiveTrueOrderByCreatedAtDesc();

    // 일반 공지사항만 조회 (중요공지 제외)
    List<Post> findByIsNoticeTrueAndIsPinnedFalseAndIsActiveTrueOrderByCreatedAtDesc();

    // 공지사항 제목으로 검색
    Page<Post> findByIsNoticeTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    // 공지사항 작성자로 검색
    Page<Post> findByIsNoticeTrueAndAuthorContainingIgnoreCase(String author, Pageable pageable);

    // 공지사항 내용으로 검색
    Page<Post> findByIsNoticeTrueAndContentContainingIgnoreCase(String content, Pageable pageable);

    // 공지사항 키워드 검색 (제목 + 내용)
    Page<Post> findByIsNoticeTrueAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    // 공지사항 상태별 조회
    Page<Post> findByIsNoticeTrueAndIsActiveOrderByIsPinnedDescCreatedAtDesc(Boolean isActive, Pageable pageable);

    // 공지사항 중요도별 조회
    Page<Post> findByIsNoticeTrueAndIsPinnedOrderByCreatedAtDesc(Boolean isPinned, Pageable pageable);

    // 만료된 공지사항 조회
    @Query("SELECT p FROM Post p WHERE p.isNotice = true AND p.expiryDate IS NOT NULL AND p.expiryDate < :now")
    List<Post> findExpiredNotices(@Param("now") LocalDateTime now);

    // 만료 임박 공지사항 조회 (3일 이내)
    @Query("SELECT p FROM Post p WHERE p.isNotice = true AND p.expiryDate IS NOT NULL AND p.expiryDate BETWEEN :now AND :threeDaysLater AND p.isActive = true")
    List<Post> findNoticesExpiringSoon(@Param("now") LocalDateTime now, @Param("threeDaysLater") LocalDateTime threeDaysLater);

    // 만료된 공지사항 자동 비활성화
    @Modifying
    @Query("UPDATE Post p SET p.isActive = false WHERE p.isNotice = true AND p.expiryDate IS NOT NULL AND p.expiryDate < :now AND p.isActive = true")
    int deactivateExpiredNotices(@Param("now") LocalDateTime now);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // ============ 통계 쿼리들 ============

    // 공지사항 개수
    long countByIsNoticeTrue();

    // 활성 공지사항 개수
    long countByIsNoticeTrueAndIsActiveTrue();

    // 중요 공지사항 개수
    long countByIsNoticeTrueAndIsPinnedTrueAndIsActiveTrue();

    // 일반 게시글 개수
    long countByIsNoticeFalse();

    // 만료된 공지사항 개수
    @Query("SELECT COUNT(p) FROM Post p WHERE p.isNotice = true AND p.expiryDate IS NOT NULL AND p.expiryDate < :now")
    long countExpiredNotices(@Param("now") LocalDateTime now);

    // ============ 통합 쿼리들 ============

    // 전체 게시글 조회 (공지사항 우선, 그 다음 일반 게시글)
    @Query("SELECT p FROM Post p ORDER BY p.isNotice DESC, p.isPinned DESC, p.createdAt DESC")
    Page<Post> findAllOrderByNoticeAndPinnedAndCreatedAt(Pageable pageable);

    // 활성 게시글만 조회 (공지사항 우선)
    @Query("SELECT p FROM Post p WHERE (p.isNotice = false) OR (p.isNotice = true AND p.isActive = true) ORDER BY p.isNotice DESC, p.isPinned DESC, p.createdAt DESC")
    Page<Post> findActivePostsOrderByNoticeAndPinned(Pageable pageable);
}