package com.forumensak.api.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FilesStorageServiceImpl implements FilesStorageService {

    // private final Path root = Paths.get("src/main/upload/static/images");
    private final Path root = Paths.get("static/images");

    @Override
    public void init() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public void save(MultipartFile file) {
        try {

            if (file.getOriginalFilename().endsWith(".jpeg") || file.getOriginalFilename().endsWith(".jpg")
                    || file.getOriginalFilename().endsWith(".png") || file.getOriginalFilename().endsWith(".tif")) {
                if (file.getSize() > 1000000) {
                    throw new RuntimeException("Could not store the file. Error: File tool large ");
                }
                Files.deleteIfExists(this.root.resolve(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
            } else {
                throw new RuntimeException("Could not store the file. Error: Not the right file type ");
            }
            System.out.println(file.getOriginalFilename());
            System.out.println(file.getOriginalFilename());
            System.out.println(file.getSize());
            System.out.println(file.getContentType());

        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }
}
