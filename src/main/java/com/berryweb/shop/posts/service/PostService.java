package com.berryweb.shop.posts.service;

import com.berryweb.shop.posts.entity.Post;
import com.berryweb.shop.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    // 모든 게시글 조회 (페이징)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // 게시글 상세 조회
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
    }

    // 게시글 생성
    @Transactional
    public Post createPost(String title, String content, String author) {
        Post post = new Post(title, content, author);
        return postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public Post updatePost(Long id, String title, String content) {
        Post post = getPostById(id);
        post.setTitle(title);
        post.setContent(content);
        return postRepository.save(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }

    // 제목으로 검색
    public Page<Post> searchByTitle(String title, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    // 작성자로 검색
    public Page<Post> searchByAuthor(String author, Pageable pageable) {
        return postRepository.findByAuthorContainingIgnoreCase(author, pageable);
    }

    // 제목 또는 내용으로 검색
    public Page<Post> searchByTitleOrContent(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    // 파일이 첨부된 게시글 조회
    public Page<Post> getPostsWithFiles(Pageable pageable) {
        return postRepository.findPostsWithFiles(pageable);
    }

    // 댓글이 있는 게시글 조회
    public List<Post> getPostsWithComments() {
        return postRepository.findPostsWithComments();
    }

    // 작성자별 게시글 개수
    public long getPostCountByAuthor(String author) {
        return postRepository.countByAuthor(author);
    }

    // 전체 게시글 개수
    public long getTotalPostCount() {
        return postRepository.count();
    }

}
