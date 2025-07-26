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

    // 특정 게시글의 파일들 조회
    public List<PostFile> getFilesByPostId(Long postId) {
        return postFileRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    // 파일 상세 조회
    public PostFile getFileById(Long id) {
        return postFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. ID: " + id));
    }

    // 저장된 파일명으로 파일 조회
    public PostFile getFileByStoredName(String storedName) {
        return postFileRepository.findByStoredName(storedName)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. 저장명: " + storedName));
    }

    // 파일 업로드
    @Transactional
    public PostFile uploadFile(Long postId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

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

        return postFileRepository.save(fileEntity);
    }

    // 파일 삭제
    @Transactional
    public void deleteFile(Long id) throws IOException {
        PostFile fileEntity = getFileById(id);

        // 실제 파일 삭제
        Path filePath = Paths.get(fileEntity.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // DB에서 파일 정보 삭제
        postFileRepository.delete(fileEntity);
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
    public long getFileCountByPostId(Long postId) {
        return postFileRepository.countByPostId(postId);
    }

    // 이미지 파일만 조회
    public List<PostFile> getImageFiles() {
        return postFileRepository.findImageFiles();
    }

    // 총 파일 용량
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
