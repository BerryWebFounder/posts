package com.berryweb.shop.posts.dto;

import lombok.Data;

@Data
public class PostCreateReq {

    private String title;
    private String content;
    private String author;

    @Override
    public String toString() {
        return "PostCreateRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                '}';
    }

}
