package com.berryweb.shop.posts.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostUpdateReq {

    private String title;
    private String content;

    // 공지사항 관련 필드 추가
    private Boolean isNotice;
    private Boolean isPinned;
    private Boolean isActive;
    private LocalDateTime expiryDate;
    private Boolean sendNotification;

}
