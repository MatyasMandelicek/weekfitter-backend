package com.weekfitter.weekfitter_backend.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

import java.io.*;
import java.nio.file.*;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000") // nebo "*"
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            Path path = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Vrátíme relativní URL pro uložení do DB:
            return ResponseEntity.ok("/api/files/" + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload error: " + e.getMessage());
        }
    }

    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String name) throws IOException {
        Path path = Paths.get(UPLOAD_DIR).resolve(name);
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        Resource resource = new org.springframework.core.io.UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .body(resource);
    }
}

