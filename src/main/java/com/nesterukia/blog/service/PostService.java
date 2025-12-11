package com.nesterukia.blog.service;

import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts(String search, Integer pageNumber, Integer pageSize) {
        return postRepository.findByTextContainsIgnoreCase(search, PageRequest.of(pageNumber, pageSize));
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No Post found with id = " + id)
        );
    }
}
