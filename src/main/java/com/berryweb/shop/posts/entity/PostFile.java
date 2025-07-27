package com.berryweb.shop.posts.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
public class PostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // JSON 직렬화 시 순환 참조 방지를 위해 @JsonIgnore 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    // postId 필드 추가 (JSON 응답에 포함할 용도)
    @Column(name = "post_id", insertable = false, updatable = false)
    private Long postId;

    public PostFile(String originalName, String storedName, String filePath, long fileSize, String contentType, Post post) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.post = post;
        this.postId = post.getId(); // postId 설정
    }

    // 파일 다운로드 URL 생성
    public String getDownloadUrl() {
        return "/api/files/download/" + this.storedName;
    }

    // 파일 타입 확인
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    // 파일 크기를 읽기 쉬운 형태로 변환
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

}
