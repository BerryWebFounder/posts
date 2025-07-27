package com.berryweb.shop.posts.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostCreateReq {

    private String title;
    private String content;
    private String author;

    // 공지사항 관련 필드 추가
    private Boolean isNotice = false;
    private Boolean isPinned = false;
    private Boolean isActive = true;
    private LocalDateTime expiryDate;
    private Boolean sendNotification = false;

    @Override
    public String toString() {
        return "PostCreateReq{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", isNotice=" + isNotice +
                ", isPinned=" + isPinned +
                ", isActive=" + isActive +
                ", expiryDate=" + expiryDate +
                ", sendNotification=" + sendNotification +
                '}';
    }

}