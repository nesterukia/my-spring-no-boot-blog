package com.nesterukia.blog.controller;

import com.nesterukia.blog.dto.PostDto;
import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(produces = "application/json")
    public List<PostDto> getAllPosts(
            @RequestParam(name = "search") String search,
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize) {
        return postService.getAllPosts(search, pageNumber, pageSize).stream().map(PostDto::fromPost).toList();
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public PostDto getPostById(@PathVariable(name = "id") Long id) {
        return PostDto.fromPost(postService.getPostById(id));
    }
}
