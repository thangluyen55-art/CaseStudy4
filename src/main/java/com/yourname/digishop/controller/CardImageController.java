package com.yourname.digishop.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class CardImageController {

    @Value("${app.card-image-dir}")
    private String imageDir;

    @PostConstruct
    void checkDirectory() {
        Path base = Paths.get(imageDir).toAbsolutePath().normalize();
        log.info("[ANH THE] Thu muc: {} | ton tai: {}", base, Files.isDirectory(base));
    }

    @GetMapping("/card-images/{filename:.+}")
    public ResponseEntity<Resource> image(@PathVariable String filename) {
        Path base = Paths.get(imageDir).toAbsolutePath().normalize();
        Path file = base.resolve(filename).normalize();

        // Chặn truy cập ra ngoài thư mục ảnh (ví dụ ../)
        if (!file.startsWith(base) || !Files.isReadable(file) || Files.isDirectory(file)) {
            log.warn("[ANH THE] Khong tim thay: {}", file);
            return ResponseEntity.notFound().build();
        }

        String name = filename.toLowerCase();
        MediaType type = name.endsWith(".webp") ? MediaType.parseMediaType("image/webp")
                : name.endsWith(".png") ? MediaType.IMAGE_PNG
                  : name.endsWith(".jpg") || name.endsWith(".jpeg") ? MediaType.IMAGE_JPEG
                    : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(new FileSystemResource(file));
    }
}