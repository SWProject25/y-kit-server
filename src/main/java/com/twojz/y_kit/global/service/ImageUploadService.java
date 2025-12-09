package com.twojz.y_kit.global.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ImageUploadService {
    @Value("${UPLOAD_PATH}")
    private String uploadPath;

    @Value("${UPLOAD_BASE_URL}")
    private String baseUrl;

    public String uploadImage(MultipartFile file, String category) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif만 가능)");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        try {
            String datePath = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy/MM")
            );
            Path categoryPath = Paths.get(uploadPath, category, datePath);
            Files.createDirectories(categoryPath);

            String fileName = UUID.randomUUID() + "." + extension;
            Path filePath = categoryPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = baseUrl + "/" + category + "/" + datePath + "/" + fileName;
            log.info("이미지 업로드 완료: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            String filePath = imageUrl.replace(baseUrl, uploadPath);
            Path path = Paths.get(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("이미지 삭제 완료: {}", imageUrl);
            }
        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", imageUrl, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String extension) {
        return extension.matches("^(jpg|jpeg|png|gif)$");
    }
}
