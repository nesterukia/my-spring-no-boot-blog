package com.nesterukia.blog.controller;

import com.nesterukia.blog.exceptions.MandatoryParameterAbsentException;
import com.nesterukia.blog.service.ImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/posts")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PutMapping("/{postId}/image")
    public void updateImage(@PathVariable(name = "postId") Long postId, @RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            throw new MandatoryParameterAbsentException("image file");
        }
        imageService.upload(postId, image);
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable(name = "postId") Long postId) throws IOException {
        Resource resource = imageService.download(postId);
        byte[] imageBytes = resource.getContentAsByteArray();

        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource.getFilename())
                .orElse(MediaType.IMAGE_JPEG);

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .contentLength(imageBytes.length)
                .body(imageBytes);
    }
}
