package com.gamemarket.controller;

import com.gamemarket.entity.Player;
import com.gamemarket.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @Autowired
    private PlayerRepository playerRepository;

    private static final String UPLOAD_DIR = "./uploads/";

    @PostMapping("/avatar")
    public Map<String, String> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Integer userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path path = Paths.get(UPLOAD_DIR + newFilename);
            Files.write(path, file.getBytes());

            // Return relative path, let frontend/proxy handle the domain
            String avatarUrl = "/uploads/" + newFilename;

            Player player = playerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            player.setAvatar(avatarUrl);
            playerRepository.save(player);

            return Map.of("url", avatarUrl);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload file");
        }
    }
}
