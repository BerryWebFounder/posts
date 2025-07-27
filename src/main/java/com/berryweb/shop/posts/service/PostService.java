package com.berryweb.shop.posts.service;

import com.berryweb.shop.posts.entity.Post;
import com.berryweb.shop.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    // ============ 기존 메서드들 (일반 게시글) ============

    // 일반 게시글만 조회 (공지사항 제외)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findByIsNoticeFalseOrderByCreatedAtDesc(pageable);
    }

    // 전체 게시글 조회 (공지사항 + 일반 게시글, 공지사항 우선)
    public Page<Post> getAllPostsWithNotices(Pageable pageable) {
        return postRepository.findActivePostsOrderByNoticeAndPinned(pageable);
    }

    // 게시글 상세 조회
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
    }

    // 게시글 상세 조회 (조회수 증가)
    @Transactional
    public Post getPostByIdWithViewCount(Long id) {
        Post post = getPostById(id);
        postRepository.incrementViewCount(id);
        post.incrementViewCount(); // 엔티티도 업데이트
        return post;
    }

    // 일반 게시글 생성
    @Transactional
    public Post createPost(String title, String content, String author) {
        Post post = new Post(title, content, author);
        return postRepository.save(post);
    }

    // 게시글 수정 (기존)
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

    // 제목으로 검색 (일반 게시글)
    public Page<Post> searchByTitle(String title, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    // 작성자로 검색 (일반 게시글)
    public Page<Post> searchByAuthor(String author, Pageable pageable) {
        return postRepository.findByAuthorContainingIgnoreCase(author, pageable);
    }

    // 제목 또는 내용으로 검색 (일반 게시글)
    public Page<Post> searchByTitleOrContent(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    // ============ 공지사항 관련 메서드들 ============

    // 공지사항 생성
    @Transactional
    public Post createNotice(String title, String content, String author, Boolean isPinned,
                             Boolean isActive, LocalDateTime expiryDate, Boolean sendNotification) {
        Post notice = new Post(title, content, author, true, isPinned, isActive, expiryDate, sendNotification);
        Post savedNotice = postRepository.save(notice);

        // 알림 발송 로직 (추후 구현)
        if (Boolean.TRUE.equals(sendNotification) && Boolean.TRUE.equals(isActive)) {
            sendNotificationToUsers(savedNotice);
        }

        return savedNotice;
    }

    // 공지사항 수정
    @Transactional
    public Post updateNotice(Long id, String title, String content, Boolean isPinned,
                             Boolean isActive, LocalDateTime expiryDate, Boolean sendNotification) {
        Post notice = getPostById(id);

        if (!notice.isNotice()) {
            throw new IllegalArgumentException("일반 게시글은 공지사항으로 수정할 수 없습니다.");
        }

        if (title != null) notice.setTitle(title);
        if (content != null) notice.setContent(content);
        if (isPinned != null) notice.setIsPinned(isPinned);
        if (isActive != null) notice.setIsActive(isActive);
        if (expiryDate != null) notice.setExpiryDate(expiryDate);
        if (sendNotification != null) notice.setSendNotification(sendNotification);

        return postRepository.save(notice);
    }

    // 통합 게시글 수정 (일반/공지사항 모두 지원)
    @Transactional
    public Post updatePostWithNoticeFields(Long id, String title, String content, Boolean isNotice,
                                           Boolean isPinned, Boolean isActive, LocalDateTime expiryDate, Boolean sendNotification) {
        Post post = getPostById(id);

        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (isNotice != null) post.setIsNotice(isNotice);
        if (isPinned != null) post.setIsPinned(isPinned);
        if (isActive != null) post.setIsActive(isActive);
        if (expiryDate != null) post.setExpiryDate(expiryDate);
        if (sendNotification != null) post.setSendNotification(sendNotification);

        return postRepository.save(post);
    }

    // 공지사항 상태 토글
    @Transactional
    public Post toggleNoticeStatus(Long id) {
        Post notice = getPostById(id);

        if (!notice.isNotice()) {
            throw new IllegalArgumentException("일반 게시글은 상태를 토글할 수 없습니다.");
        }

        notice.setIsActive(!notice.getIsActive());
        return postRepository.save(notice);
    }

    // 활성 공지사항 조회 (페이징)
    public Page<Post> getActiveNotices(Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(pageable);
    }

    // 활성 공지사항 조회 (List) - 메인 페이지용
    public List<Post> getActiveNoticesList() {
        return postRepository.findByIsNoticeTrueAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc();
    }

    // 전체 공지사항 조회 (관리자용)
    public Page<Post> getAllNotices(Pageable pageable) {
        return postRepository.findByIsNoticeTrueOrderByIsPinnedDescCreatedAtDesc(pageable);
    }

    // 중요 공지사항만 조회
    public List<Post> getPinnedNotices() {
        return postRepository.findByIsNoticeTrueAndIsPinnedTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }

    // 일반 공지사항만 조회 (중요공지 제외)
    public List<Post> getRegularNotices() {
        return postRepository.findByIsNoticeTrueAndIsPinnedFalseAndIsActiveTrueOrderByCreatedAtDesc();
    }

    // 공지사항 검색 메서드들
    public Page<Post> searchNoticesByTitle(String title, Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndTitleContainingIgnoreCase(title, pageable);
    }

    public Page<Post> searchNoticesByAuthor(String author, Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndAuthorContainingIgnoreCase(author, pageable);
    }

    public Page<Post> searchNoticesByContent(String content, Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndContentContainingIgnoreCase(content, pageable);
    }

    public Page<Post> searchNoticesByKeyword(String keyword, Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    // 공지사항 상태별 조회
    public Page<Post> getNoticesByStatus(Boolean isActive, Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndIsActiveOrderByIsPinnedDescCreatedAtDesc(isActive, pageable);
    }

    // 공지사항 중요도별 조회
    public Page<Post> getNoticesByPinned(Boolean isPinned, Pageable pageable) {
        return postRepository.findByIsNoticeTrueAndIsPinnedOrderByCreatedAtDesc(isPinned, pageable);
    }

    // 만료 임박 공지사항 조회
    public List<Post> getNoticesExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        return postRepository.findNoticesExpiringSoon(now, threeDaysLater);
    }

    // 만료된 공지사항 자동 비활성화 (스케줄러)
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @Transactional
    public void deactivateExpiredNotices() {
        LocalDateTime now = LocalDateTime.now();
        int deactivatedCount = postRepository.deactivateExpiredNotices(now);
        if (deactivatedCount > 0) {
            System.out.println("만료된 공지사항 " + deactivatedCount + "개를 비활성화했습니다.");
        }
    }

    // ============ 기존 메서드들 ============

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

    // ============ 통계 메서드들 ============

    // 전체 게시글 개수
    public long getTotalPostCount() {
        return postRepository.count();
    }

    // 일반 게시글 개수
    public long getRegularPostCount() {
        return postRepository.countByIsNoticeFalse();
    }

    // 공지사항 개수
    public long getNoticeCount() {
        return postRepository.countByIsNoticeTrue();
    }

    // 활성 공지사항 개수
    public long getActiveNoticeCount() {
        return postRepository.countByIsNoticeTrueAndIsActiveTrue();
    }

    // 중요 공지사항 개수
    public long getPinnedNoticeCount() {
        return postRepository.countByIsNoticeTrueAndIsPinnedTrueAndIsActiveTrue();
    }

    // 만료된 공지사항 개수
    public long getExpiredNoticeCount() {
        return postRepository.countExpiredNotices(LocalDateTime.now());
    }

    // 알림 발송 (추후 구현)
    private void sendNotificationToUsers(Post notice) {
        // 실제 알림 발송 로직 구현
        // 예: 이메일, 푸시 알림, SMS 등
        System.out.println("새 공지사항 알림 발송: " + notice.getTitle());
    }

}