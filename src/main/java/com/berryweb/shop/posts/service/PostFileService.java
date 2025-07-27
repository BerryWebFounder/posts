package com.berryweb.shop.posts.service;

import com.berryweb.shop.posts.entity.PostFile;
import com.berryweb.shop.posts.entity.Post;
import com.berryweb.shop.posts.repository.PostFileRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFileService {

    private final PostFileRepository postFileRepository;
    private final PostService postService;

    @Value("${file.upload.directory:uploads}")
    private String uploadDirectory;

    // 특정 게시글의 파일들 조회 (트랜잭션 내에서 실행)
    @Transactional(readOnly = true)
    public List<PostFile> getFilesByPostId(Long postId) {
        System.out.println("=== 파일 조회 시작 ===");
        System.out.println("게시글 ID: " + postId);

        List<PostFile> files = postFileRepository.findByPostIdOrderByCreatedAtAsc(postId);

        System.out.println("조회된 파일 개수: " + files.size());

        // 각 파일의 postId를 명시적으로 설정 (LAZY 로딩 이슈 방지)
        files.forEach(file -> {
            if (file.getPostId() == null && file.getPost() != null) {
                file.setPostId(file.getPost().getId());
            }
            System.out.println("파일: " + file.getOriginalName() + " (크기: " + file.getFormattedFileSize() + ")");
        });

        System.out.println("=== 파일 조회 완료 ===");
        return files;
    }

    // 파일 상세 조회
    @Transactional(readOnly = true)
    public PostFile getFileById(Long id) {
        PostFile file = postFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. ID: " + id));

        // postId 설정 확인
        if (file.getPostId() == null && file.getPost() != null) {
            file.setPostId(file.getPost().getId());
        }

        return file;
    }

    // 저장된 파일명으로 파일 조회
    @Transactional(readOnly = true)
    public PostFile getFileByStoredName(String storedName) {
        PostFile file = postFileRepository.findByStoredName(storedName)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. 저장명: " + storedName));

        // postId 설정 확인
        if (file.getPostId() == null && file.getPost() != null) {
            file.setPostId(file.getPost().getId());
        }

        return file;
    }

    // 파일 업로드
    @Transactional
    public PostFile uploadFile(Long postId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        System.out.println("=== 파일 업로드 시작 ===");
        System.out.println("게시글 ID: " + postId);
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("파일 크기: " + file.getSize());
        System.out.println("Content Type: " + file.getContentType());

        Post post = postService.getPostById(postId);

        // 저장할 파일명 생성 (UUID + 원본 확장자)
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String storedName = UUID.randomUUID().toString() + extension;

        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath);

        // 파일 정보 DB 저장
        PostFile fileEntity = new PostFile(
                originalName,
                storedName,
                filePath.toString(),
                file.getSize(),
                file.getContentType(),
                post
        );

        PostFile savedFile = postFileRepository.save(fileEntity);
        System.out.println("파일 업로드 완료: " + savedFile.getOriginalName());
        System.out.println("=== 파일 업로드 완료 ===");

        return savedFile;
    }

    // 파일 삭제
    @Transactional
    public void deleteFile(Long id) throws IOException {
        PostFile fileEntity = getFileById(id);

        // 실제 파일 삭제
        Path filePath = Paths.get(fileEntity.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("물리적 파일 삭제 완료: " + fileEntity.getOriginalName());
        }

        // DB에서 파일 정보 삭제
        postFileRepository.delete(fileEntity);
        System.out.println("DB에서 파일 정보 삭제 완료");
    }

    // 파일 다운로드용 바이트 배열 반환
    public byte[] downloadFile(String storedName) throws IOException {
        PostFile postFile = getFileByStoredName(storedName);
        Path filePath = Paths.get(postFile.getFilePath());

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("파일이 존재하지 않습니다: " + storedName);
        }

        return Files.readAllBytes(filePath);
    }

    // 특정 게시글의 파일 개수
    @Transactional(readOnly = true)
    public long getFileCountByPostId(Long postId) {
        return postFileRepository.countByPostId(postId);
    }

    // 이미지 파일만 조회
    @Transactional(readOnly = true)
    public List<PostFile> getImageFiles() {
        List<PostFile> files = postFileRepository.findImageFiles();

        // postId 설정
        files.forEach(file -> {
            if (file.getPostId() == null && file.getPost() != null) {
                file.setPostId(file.getPost().getId());
            }
        });

        return files;
    }

    // 총 파일 용량
    @Transactional(readOnly = true)
    public Long getTotalFileSize() {
        Long totalSize = postFileRepository.getTotalFileSize();
        return totalSize != null ? totalSize : 0L;
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // 파일 크기를 읽기 쉬운 형태로 변환
    public String formatFileSize(Long fileSize) {
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

    @PostConstruct
    public void initializeUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            System.out.println("업로드 경로: " + uploadPath.toAbsolutePath());

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("업로드 폴더 생성 완료!");
            }

            // 쓰기 권한 테스트
            Path testFile = uploadPath.resolve("test.tmp");
            Files.write(testFile, "test".getBytes());
            Files.delete(testFile);
            System.out.println("업로드 폴더 쓰기 권한 확인 완료!");

        } catch (IOException e) {
            System.err.println("업로드 폴더 설정 실패: " + e.getMessage());
        }
    }

}
