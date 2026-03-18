package com.sesame.neobte.Services.Other;

import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB

    public String storeActualiteImage(MultipartFile file) {
        return storeImage(file, "actualites");
    }

    public String storeProfileImage(MultipartFile file) {
        return storeImage(file, "profiles");
    }

    private String storeImage(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Image trop volumineuse (max 5 Mo).");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("Fichier invalide: veuillez envoyer une image.");
        }

        String ext = extensionFromFilename(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext != null ? ("." + ext) : "");

        Path dir = Paths.get("uploads", subdir);
        try {
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Impossible d'enregistrer l'image.");
        }

        return "/uploads/" + subdir + "/" + filename;
    }

    private String extensionFromFilename(String name) {
        if (name == null) return null;
        String n = name.trim();
        int idx = n.lastIndexOf('.');
        if (idx < 0 || idx == n.length() - 1) return null;
        String ext = n.substring(idx + 1).toLowerCase(Locale.ROOT);
        if (ext.length() > 8) return null;
        return ext.replaceAll("[^a-z0-9]", "");
    }
}

