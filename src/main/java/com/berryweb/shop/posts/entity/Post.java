package com.berryweb.shop.posts.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String author;

    // 공지사항 관련 필드 추가
    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice = false;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "send_notification", nullable = false)
    private Boolean sendNotification = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JSON 직렬화에서 제외 (순환 참조 방지)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    // JSON 직렬화에서 제외 (순환 참조 방지)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostFile> files = new ArrayList<>();

    // 기존 생성자 (일반 게시글용)
    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.isNotice = false;
        this.isPinned = false;
        this.isActive = true;
        this.viewCount = 0L;
        this.sendNotification = false;
    }

    // 공지사항용 생성자
    public Post(String title, String content, String author, Boolean isNotice, Boolean isPinned,
                Boolean isActive, LocalDateTime expiryDate, Boolean sendNotification) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.isNotice = isNotice != null ? isNotice : false;
        this.isPinned = isPinned != null ? isPinned : false;
        this.isActive = isActive != null ? isActive : true;
        this.expiryDate = expiryDate;
        this.viewCount = 0L;
        this.sendNotification = sendNotification != null ? sendNotification : false;
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 만료된 공지사항은 자동으로 비활성화
    @PreUpdate
    public void checkExpiry() {
        if (isNotice && isExpired()) {
            this.isActive = false;
        }
    }

    // 공지사항 여부 확인
    public boolean isNotice() {
        return Boolean.TRUE.equals(this.isNotice);
    }

    // 중요 공지사항 여부 확인
    public boolean isPinned() {
        return Boolean.TRUE.equals(this.isPinned);
    }

    // 활성 상태 확인
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    // 댓글 개수 반환 (편의 메서드)
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    // 파일 개수 반환 (편의 메서드)
    public int getFileCount() {
        return files != null ? files.size() : 0;
    }

    // 파일 첨부 여부 확인
    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    // 댓글 존재 여부 확인
    public boolean hasComments() {
        return comments != null && !comments.isEmpty();
    }

}
