package com.recruitment.service;

import com.recruitment.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ResumeFileStorageService {

    private final Path rootDir;

    public ResumeFileStorageService(@Value("${app.upload-dir:uploads/resumes}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建简历存储目录: " + rootDir, e);
        }
    }

    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "简历文件为空");
        }
        return storeBytes(file.getBytes(), file.getOriginalFilename(), file.getContentType());
    }

    public StoredFile storeBytes(byte[] data, String originalName, String contentType) throws IOException {
        if (data == null || data.length == 0) {
            throw new BusinessException(400, "简历文件为空");
        }
        String name = originalName != null ? originalName : "resume.bin";
        String storedName = UUID.randomUUID() + "_" + sanitize(name);
        Path target = rootDir.resolve(storedName);
        Files.write(target, data);
        if (contentType == null || contentType.isBlank()) {
            contentType = guessContentType(name);
        }
        return new StoredFile(name, storedName, contentType);
    }

    public Resource loadAsResource(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            throw new BusinessException(404, "未上传原始简历文件");
        }
        Path file = rootDir.resolve(storedName).normalize();
        if (!file.startsWith(rootDir) || !Files.exists(file)) {
            throw new BusinessException(404, "简历原文件不存在");
        }
        return new FileSystemResource(file);
    }

    private String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String guessContentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }

    public record StoredFile(String originalName, String storedName, String contentType) {}
}
