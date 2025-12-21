package com.nesterukia.blog.controller;

import com.nesterukia.blog.dto.post.PostDto;
import com.nesterukia.blog.dto.post.CreatePostDto;
import com.nesterukia.blog.dto.post.PostPageResponse;
import com.nesterukia.blog.dto.post.UpdatePostDto;
import com.nesterukia.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
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

    @PostMapping("/{postId}/likes")
    public int incrementLikesCount(@PathVariable(name = "postId") Long postId){
        return postService.incrementAndGetLikesCount(postId);
    }
}
