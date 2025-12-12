package com.nesterukia.blog.service;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Image;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final PostService postService;

    @Autowired
    public ImageService(ImageRepository imageRepository, PostService postService) {
        this.imageRepository = imageRepository;
        this.postService = postService;
    }

    @Value("${blog.uploads.images.directory:uploads/}")
    private String
            UPLOAD_DIR;

    public void upload(Long postId, MultipartFile file) {
        try {
            Path uploadDir = Paths.get(UPLOAD_DIR);
            Path filePath = uploadDir.resolve(buildImageFilenameByPostId(postId, file));
            file.transferTo(filePath);
            Post postOfImage = postService.getPostById(postId);
            imageRepository.save(
                    new Image(filePath.toString(), postOfImage.getId())
            );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Resource download(Long postId) {
        Image image = imageRepository.findByPostId(postId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Image for postId = '%s' was not found.", postId)
                )
        );
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(image.getSource()).normalize();
            byte[] content = Files.readAllBytes(filePath);

            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String buildImageFilenameByPostId(Long postId, MultipartFile file) {
        return "image-of-post-" + postId + getFileExtension(file);
    }

    private static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

}
