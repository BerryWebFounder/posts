package com.berryweb.shop.posts.controller;

import com.berryweb.shop.posts.entity.PostFile;
import com.berryweb.shop.posts.service.PostFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final PostFileService postFileService;

    // 특정 게시글의 파일 목록 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostFile>> getFilesByPostId(@PathVariable Long postId) {
        List<PostFile> files = postFileService.getFilesByPostId(postId);
        return ResponseEntity.ok(files);
    }

    // 파일 업로드
    @PostMapping("/upload/{postId}")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @PathVariable Long postId,
            @RequestParam("files") MultipartFile[] files) {

        System.out.println("=== 파일 업로드 시작 ===");
        System.out.println("게시글 ID: " + postId);
        System.out.println("받은 파일 개수: " + files.length);

        Map<String, Object> response = new HashMap<>();

        try {
            List<PostFile> uploadedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    System.out.println("처리 중인 파일: " + file.getOriginalFilename());
                    PostFile postFile = postFileService.uploadFile(postId, file);
                    uploadedFiles.add(postFile);
                }
            }

            response.put("message", uploadedFiles.size() + "개 파일 업로드 완료");
            response.put("files", uploadedFiles);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("업로드 실패: " + e.getMessage());
            e.printStackTrace();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 다중 파일 업로드
    @PostMapping("/upload-multiple/{postId}")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @PathVariable Long postId,
            @RequestParam("files") MultipartFile[] files) {

        Map<String, Object> response = new HashMap<>();

        try {
            for (MultipartFile file : files) {
                postFileService.uploadFile(postId, file);
            }

            response.put("message", files.length + "개의 파일이 성공적으로 업로드되었습니다.");
            response.put("count", files.length);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 파일 다운로드
    @GetMapping("/download/{storedName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String storedName) {
        try {
            PostFile fileEntity = postFileService.getFileByStoredName(storedName);
            byte[] fileData = postFileService.downloadFile(storedName);

            ByteArrayResource resource = new ByteArrayResource(fileData);

            // 파일명 인코딩 (한글 파일명 지원)
            String encodedFileName = URLEncoder.encode(fileEntity.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileData.length)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 파일 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable Long id) {
        try {
            postFileService.deleteFile(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "파일이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 이미지 파일 목록 조회
    @GetMapping("/images")
    public ResponseEntity<List<PostFile>> getImageFiles() {
        List<PostFile> imageFiles = postFileService.getImageFiles();
        return ResponseEntity.ok(imageFiles);
    }

    // 파일 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable Long id) {
        PostFile postFile = postFileService.getFileById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("file", postFile);
        response.put("formattedSize", postFileService.formatFileSize(postFile.getFileSize()));

        return ResponseEntity.ok(response);
    }

}
