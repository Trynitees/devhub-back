package com.devhub.devhub.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir){
        this.root = Paths.get(uploadDir).toAbsolutePath();
    }

    public String save(MultipartFile file, String folder){
        String original = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean imageType = contentType != null && contentType.startsWith("image/");
        boolean imageExt = original != null
                && original.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)");
        if (!imageType && !imageExt) {
            throw new ApiException(HttpStatus.BAD_REQUEST, file.getName(), "Нужно загрузить изображение");
        }
        try {
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String name = UUID.randomUUID() + ext;
            Path dir = root.resolve(folder);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return "/media/" + folder + "/" + name;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "detail", "Не удалось сохранить файл");
        }
    }

}
