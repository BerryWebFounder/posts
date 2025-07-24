package com.berryweb.shop.posts.repository;

import com.berryweb.shop.posts.entity.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostFileRepository extends JpaRepository<PostFile, Long> {

    // 특정 게시글의 파일들 조회
    List<PostFile> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 저장된 파일명으로 파일 조회
    Optional<PostFile> findByStoredName(String storedName);

    // 원본 파일명으로 검색
    List<PostFile> findByOriginalNameContainingIgnoreCase(String originalName);

    // 특정 게시글의 파일 개수
    long countByPostId(Long postId);

    // 특정 확장자 파일들 조회
    @Query("SELECT f FROM PostFile f WHERE f.originalName LIKE %:extension")
    List<PostFile> findByFileExtension(@Param("extension") String extension);

    // 파일 크기별 조회 (예: 1MB 이상)
    List<PostFile> findByFileSizeGreaterThan(Long fileSize);

    // 특정 Content-Type 파일들 조회
    List<PostFile> findByContentType(String contentType);

    // 이미지 파일만 조회
    @Query("SELECT f FROM PostFile f WHERE f.contentType LIKE 'image/%'")
    List<PostFile> findImageFiles();

    // 총 파일 용량 계산
    @Query("SELECT SUM(f.fileSize) FROM PostFile f")
    Long getTotalFileSize();

    // 게시글별 파일 개수 조회 (통계용)
    @Query("SELECT f.post.id, COUNT(f) FROM PostFile f GROUP BY f.post.id")
    List<Object[]> countFilesByPost();

}
