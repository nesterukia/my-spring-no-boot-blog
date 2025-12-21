package com.nesterukia.blog.unit.service;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Image;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.ImageRepository;
import com.nesterukia.blog.service.ImageService;
import com.nesterukia.blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PostService postService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageService imageService;

    @TempDir
    Path tempDir;

    private final Long POST_ID = 1L;
    private Post testPost;
    private Image testImage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "UPLOAD_DIR", tempDir.toString());

        testPost = Post.builder()
                .id(POST_ID)
                .title("Test Post")
                .build();

        testImage = new Image("image-of-post-1.jpg", POST_ID);
        testImage.setId(1L);
    }

    @Test
    void testUploadSuccessfully() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        imageService.upload(POST_ID, multipartFile);

        verify(postService).getPostById(POST_ID);
        verify(imageRepository).save(any(Image.class));
        verify(multipartFile).transferTo(any(Path.class));
    }

    @Test
    void testUploadCreatesUploadDirectory() {
        Path newUploadDir = tempDir.resolve("new_uploads");
        ReflectionTestUtils.setField(imageService, "UPLOAD_DIR", newUploadDir.toString());

        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        imageService.upload(POST_ID, multipartFile);

        assertThat(Files.exists(newUploadDir)).isTrue();
    }

    @Test
    void testUploadBuildCorrectFilename() {
        when(multipartFile.getOriginalFilename()).thenReturn("test.png");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        imageService.upload(POST_ID, multipartFile);

        verify(imageRepository).save(argThat(image ->
                image.getSource().equals("image-of-post-1.png")
        ));
    }

    @Test
    void testUploadThrowsExceptionWhenPostNotFound() {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(postService.getPostById(POST_ID)).thenThrow(
                new EntityNotFoundException("No Post found")
        );

        assertThatThrownBy(() -> imageService.upload(POST_ID, multipartFile))
                .isInstanceOf(EntityNotFoundException.class);

        verify(imageRepository, never()).save(any());
    }

    @Test
    void testDownloadSuccessfully() throws IOException {
        Files.createFile(tempDir.resolve("image-of-post-1.jpg"));
        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(testImage));

        Resource resource = imageService.download(POST_ID);

        assertThat(resource).isNotNull();
        assertThat(resource.contentLength()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testDownloadThrowsExceptionWhenImageNotFound() {
        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageService.download(POST_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Image for postId = '1' was not found");
    }

    @Test
    void testDownloadThrowsExceptionWhenFileDoesNotExist() {
        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(testImage));

        assertThatThrownBy(() -> imageService.download(POST_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Image file not found at path");
    }

    @Test
    void testDownloadReadsCorrectFile() throws IOException {
        byte[] fileContent = "test image content".getBytes();
        Path filePath = tempDir.resolve("image-of-post-1.jpg");
        Files.write(filePath, fileContent);

        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(testImage));

        Resource resource = imageService.download(POST_ID);

        assertThat(resource.contentLength()).isEqualTo(fileContent.length);
    }

    @Test
    void testUploadWithDifferentExtension(){
        when(multipartFile.getOriginalFilename()).thenReturn("image.png");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        imageService.upload(POST_ID, multipartFile);

        verify(imageRepository).save(argThat(image ->
                image.getSource().endsWith(".png")
        ));
    }

    @Test
    void testUploadWithMultipleDots() {
        when(multipartFile.getOriginalFilename()).thenReturn("my.photo.jpg");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        imageService.upload(POST_ID, multipartFile);

        verify(imageRepository).save(argThat(image ->
                image.getSource().equals("image-of-post-1.jpg")
        ));
    }

    @Test
    void testDownloadWithNonexistentFile() {
        Image nonexistentImage = new Image("nonexistent-file.jpg", POST_ID);
        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(nonexistentImage));

        assertThatThrownBy(() -> imageService.download(POST_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Image file not found at path");
    }

    @Test
    void testUploadAndDownloadIntegration() throws IOException {
        byte[] fileContent = "image data".getBytes();
        Path filePath = tempDir.resolve("image-of-post-1.jpg");
        Files.write(filePath, fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);
        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(testImage));

        imageService.upload(POST_ID, multipartFile);
        Resource resource = imageService.download(POST_ID);

        assertThat(resource).isNotNull();
        assertThat(resource.contentLength()).isEqualTo(fileContent.length);
    }

    @Test
    void testDownloadWithLargeFile() throws IOException {
        byte[] largeContent = new byte[1024 * 1024];
        Path filePath = tempDir.resolve("image-of-post-1.jpg");
        Files.write(filePath, largeContent);

        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(testImage));

        Resource resource = imageService.download(POST_ID);

        assertThat(resource.contentLength()).isEqualTo(1024 * 1024);
    }

    @Test
    void testUploadSavesCorrectPostId() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(postService.getPostById(POST_ID)).thenReturn(testPost);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        imageService.upload(POST_ID, multipartFile);

        verify(imageRepository).save(argThat(image ->
                image.getPostId().equals(POST_ID)
        ));
    }

    @Test
    void testDownloadThrowsExceptionWhenReadingFileFails() {
        Image imageWithInvalidPath = new Image("non_existent_file.jpg", POST_ID);
        when(imageRepository.findByPostId(POST_ID)).thenReturn(Optional.of(imageWithInvalidPath));

        assertThatThrownBy(() -> imageService.download(POST_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Image file not found at path");
    }
}
