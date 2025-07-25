package com.berryweb.shop.posts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"com.berryweb.shop"})
public class PostsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostsApplication.class, args);
    }

}
