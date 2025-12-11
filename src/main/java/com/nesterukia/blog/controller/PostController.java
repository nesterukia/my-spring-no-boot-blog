package com.nesterukia.blog.controller;

import com.nesterukia.blog.dto.comment.CommentDto;
import com.nesterukia.blog.dto.comment.CreateCommentDto;
import com.nesterukia.blog.dto.post.PostDto;
import com.nesterukia.blog.dto.post.CreatePostDto;
import com.nesterukia.blog.dto.post.PostPageResponse;
import com.nesterukia.blog.dto.post.UpdatePostDto;
import com.nesterukia.blog.service.CommentService;
import com.nesterukia.blog.service.ImageService;
import com.nesterukia.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final ImageService imageService;

    @Autowired
    public PostController(PostService postService, CommentService commentService, ImageService imageService) {
        this.postService = postService;
        this.commentService = commentService;
        this.imageService = imageService;
    }

    @GetMapping(produces = "application/json")
    public PostPageResponse getAllPosts(
            @RequestParam(name = "search") String search,
            @RequestParam(name = "pageNumber", defaultValue = "1") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return postService.getPosts(search, pageNumber, pageSize);
    }

    @GetMapping(path = "/{postId}", produces = "application/json")
    public PostDto getPostById(@PathVariable(name = "postId") Long postId) {
        return PostDto.fromPost(postService.getPostById(postId));
    }

    @PostMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto createPost(@RequestBody CreatePostDto post) {
        return PostDto.fromPost(postService.savePost(post));
    }

    @PutMapping(path = "/{postId}",produces = "application/json")
    public PostDto updatePost(@PathVariable(name = "postId") Long postId, @RequestBody UpdatePostDto post) {
        return PostDto.fromPost(postService.updatePost(postId, post));
    }

    @DeleteMapping(path = "/{postId}", produces = "application/json")
    public void deletePostById(@PathVariable(name = "postId") Long postId) {
        postService.deletePostById(postId);
    }

    @PutMapping("/{postId}/image")
    public void updateImage(@PathVariable(name = "postId") Long postId, @RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is empty");
        }
        imageService.upload(postId, image);
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable(name = "postId") Long postId) {
        try {
            Resource resource = imageService.download(postId);
            byte[] content = resource.getContentAsByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(content);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found", e);
        }
    }

    @PostMapping("/{postId}/likes")
    public int incrementLikesCount(@PathVariable(name = "postId") Long postId){
        return postService.incrementAndGetLikesCount(postId);
    }

    @GetMapping("/{postId}/comments")
    public Set<CommentDto> getAllComments(@PathVariable(name = "postId") Long postId){
        return commentService.findAllByPostId(postId).stream()
                .map(CommentDto::fromComment)
                .collect(Collectors.toSet());
    }

    @PostMapping("/{postId}/comments")
    public CommentDto addComment(@RequestBody CreateCommentDto createCommentDto){
        return CommentDto.fromComment(commentService.save(createCommentDto));
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable(name = "postId") Long postId,
                                     @PathVariable(name = "commentId") Long commentId) {
        return CommentDto.fromComment(commentService.findCommentById(postId, commentId));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable(name = "postId") Long postId,
                                    @PathVariable(name = "commentId") Long commentId,
                                    @RequestBody CommentDto commentDto) {
        return CommentDto.fromComment(commentService.updateComment(postId, commentId, commentDto.text()));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public void updateComment(@PathVariable(name = "postId") Long postId,
                                    @PathVariable(name = "commentId") Long commentId) {
        commentService.delete(postId, commentId);
    }
}
