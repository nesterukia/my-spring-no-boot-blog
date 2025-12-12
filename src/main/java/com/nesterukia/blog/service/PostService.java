package com.nesterukia.blog.service;

import com.nesterukia.blog.dto.post.CreatePostDto;
import com.nesterukia.blog.dto.post.PostDto;
import com.nesterukia.blog.dto.post.PostPageResponse;
import com.nesterukia.blog.dto.post.UpdatePostDto;
import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.model.Tag;
import com.nesterukia.blog.repository.CommentRepository;
import com.nesterukia.blog.repository.PostRepository;
import com.nesterukia.blog.repository.PostTagRepository;
import com.nesterukia.blog.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
public class PostService {
    private static final int MAX_TEXT_LENGTH_ON_PAGE = 128;
    private static final String TRIMMED_TEXT_SUFFIX = "...";

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public PostService(PostRepository postRepository, TagRepository tagRepository, PostTagRepository postTagRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public PostPageResponse getPosts(String search, Integer pageNumber, Integer pageSize) {
        int correctedPageNumber = pageNumber - 1; // first page is counted from zero
        Page<Post> postPage = postRepository.findAllByTextContainsIgnoreCase(
                search,
                PageRequest.of(correctedPageNumber, pageSize, Sort.by("id").descending())
        );

        return new PostPageResponse(
                postPage.get()
                        .map(this::inferPostWithTagsAndComments)
                        .map(this::cropTextIfMaxLengthExceeded)
                        .map(PostDto::fromPost).toList(),
                postPage.hasPrevious(),
                postPage.hasNext(),
                postPage.getTotalPages()
        );
    }

    @Transactional
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No Post found with id = " + id)
        );

        return inferPostWithTagsAndComments(post);
    }

    @Transactional
    public Post savePost(CreatePostDto createPostDto) {
        createPostDto.validateMandatoryFields();

        Post post = postRepository.save(fromDto(createPostDto));
        Set<Tag> tags = tagRepository.saveTags(post.getId(), createPostDto.tags());
        postTagRepository.batchInsertPostTags(post.getId(), tags);
        post.setTags(tags);
        return post;
    }

    public Post updatePost(Long postId, UpdatePostDto updatePostDto) {
        updatePostDto.validateMandatoryFields();

        Post updatedPost = postRepository.update(postId, fromDto(postId, updatePostDto));
        postTagRepository.deleteByPostId(postId);
        Set<Tag> tags = tagRepository.saveTags(postId, updatePostDto.tags());
        postTagRepository.batchInsertPostTags(postId, tags);
        updatedPost.setTags(tags);
        return updatedPost;
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
        postTagRepository.deleteByPostId(id);
        commentRepository.deleteAllByPostId(id);
    }

    public int incrementAndGetLikesCount(Long postId) {
        return postRepository.incrementAndGetLikesCount(postId);
    }

    private Post fromDto(CreatePostDto postDto) {
        Post post = new Post();
        post.setTitle(postDto.title());
        post.setText(postDto.text());
        post.setLikesCount(0L);
        return post;
    }

    private Post fromDto(Long id, UpdatePostDto postDto) {
        Post post = getPostById(id);
        post.setTitle(postDto.title());
        post.setText(postDto.text());
        return post;
    }

    private Post inferPostWithTagsAndComments(Post post) {
        Set<Tag> tagsByPostId = tagRepository.findTagsByPostId(post.getId());
        Set<Comment> commentsByPostId = commentRepository.findAllByPostId(post.getId());

        post.setTags(tagsByPostId);
        post.setComments(commentsByPostId);

        return post;
    }

    private Post cropTextIfMaxLengthExceeded(Post post) {
        int textLength = post.getText().length();

        if (textLength > MAX_TEXT_LENGTH_ON_PAGE) {
            String croppedText = post.getText().substring(0, MAX_TEXT_LENGTH_ON_PAGE).concat(TRIMMED_TEXT_SUFFIX);
            post.setText(croppedText);
        }

        return post;
    }
}
