package com.berryweb.shop.posts.dto;

import lombok.Data;

@Data
public class CommentCreateReq {

    private Long postId;
    private String content;
    private String author;

}
