package com.nesterukia.blog.unit.service;

import com.nesterukia.blog.dto.post.CreatePostDto;
import com.nesterukia.blog.dto.post.PostPageResponse;
import com.nesterukia.blog.dto.post.UpdatePostDto;
import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.exceptions.MandatoryParameterAbsentException;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.model.Tag;
import com.nesterukia.blog.repository.CommentRepository;
import com.nesterukia.blog.repository.PostRepository;
import com.nesterukia.blog.repository.PostTagRepository;
import com.nesterukia.blog.repository.TagRepository;
import com.nesterukia.blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private CreatePostDto createPostDto;
    private UpdatePostDto updatePostDto;
    private Tag testTag;
    private Comment testComment;
    private final Long POST_ID = 1L;

    @BeforeEach
    void setUp() {
        testPost = Post.builder()
                .id(POST_ID)
                .title("Test Post")
                .text("Test content")
                .likesCount(0L)
                .build();

        createPostDto = new CreatePostDto("New Post", "New content", Set.of("java", "spring"));
        updatePostDto = new UpdatePostDto(1L, "Updated Post", "Updated content", Set.of("java"));

        testTag = Tag.builder()
                .id(1L)
                .title("java")
                .build();

        testComment = Comment.builder()
                .id(1L)
                .postId(POST_ID)
                .text("Comment text")
                .build();
    }

    @Test
    void testGetPostsSuccessfully() {
        List<Post> posts = List.of(testPost);
        Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);

        when(postRepository.findAllByTextContainsIgnoreCase(eq(""), any(PageRequest.class)))
                .thenReturn(postPage);
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of(testTag));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        PostPageResponse response = postService.getPosts("", 1, 10);

        assertThat(response.hasPrev()).isFalse();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void testGetPostsConvertsPageNumber() {
        Page<Post> postPage = new PageImpl<>(List.of(testPost), PageRequest.of(0, 10), 1);

        when(postRepository.findAllByTextContainsIgnoreCase(eq(""), any(PageRequest.class)))
                .thenReturn(postPage);
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of());
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        postService.getPosts("", 1, 10);

        verify(postRepository).findAllByTextContainsIgnoreCase(eq(""), argThat(pr ->
                pr.getPageNumber() == 0
        ));
    }

    @Test
    void testGetPostsCropsLongText() {
        String longText = "A".repeat(200);
        Post longPost = testPost.toBuilder().text(longText).build();
        Page<Post> postPage = new PageImpl<>(List.of(longPost), PageRequest.of(0, 10), 1);

        when(postRepository.findAllByTextContainsIgnoreCase(eq(""), any(PageRequest.class)))
                .thenReturn(postPage);
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of());
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        PostPageResponse response = postService.getPosts("", 1, 10);

        String croppedText = response.posts().get(0).text();
        assertThat(croppedText).hasSize(131).endsWith("...");
    }

    @Test
    void testGetPostsDoesNotCropShortText() {
        Page<Post> postPage = new PageImpl<>(List.of(testPost), PageRequest.of(0, 10), 1);

        when(postRepository.findAllByTextContainsIgnoreCase(eq(""), any(PageRequest.class)))
                .thenReturn(postPage);
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of());
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        PostPageResponse response = postService.getPosts("", 1, 10);

        assertThat(response.posts().get(0).text()).isEqualTo("Test content");
    }

    @Test
    void testGetPostByIdSuccessfully() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of(testTag));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of(testComment));

        Post result = postService.getPostById(POST_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTags()).contains(testTag);
        assertThat(result.getComments()).contains(testComment);
    }

    @Test
    void testGetPostByIdThrowsException() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(POST_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No Post found with id = " + POST_ID);
    }

    @Test
    void testSavePostSuccessfully() {
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(tagRepository.saveTags(POST_ID, createPostDto.tags())).thenReturn(Set.of(testTag));

        Post result = postService.savePost(createPostDto);

        assertThat(result).isNotNull();
        assertThat(result.getTags()).contains(testTag);
        verify(postRepository).save(any(Post.class));
        verify(tagRepository).saveTags(POST_ID, createPostDto.tags());
        verify(postTagRepository).batchInsertPostTags(POST_ID, Set.of(testTag));
    }

    @Test
    void testSavePostThrowsExceptionWhenValidationFails() {
        CreatePostDto invalidDto = new CreatePostDto(null, null, null);

        assertThatThrownBy(() -> postService.savePost(invalidDto))
                .isInstanceOf(MandatoryParameterAbsentException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void testSavePostSetsLikesCountToZero() {
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(tagRepository.saveTags(anyLong(), any())).thenReturn(Set.of());

        postService.savePost(createPostDto);

        verify(postRepository).save(argThat(post ->
                post.getLikesCount() == 0L
        ));
    }

    @Test
    void testUpdatePostSuccessfully() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(postRepository.update(eq(POST_ID), any(Post.class))).thenReturn(testPost);
        when(tagRepository.saveTags(POST_ID, updatePostDto.tags())).thenReturn(Set.of(testTag));

        Post result = postService.updatePost(POST_ID, updatePostDto);

        assertThat(result).isNotNull();
        verify(postTagRepository).deleteByPostId(POST_ID);
        verify(tagRepository).saveTags(POST_ID, updatePostDto.tags());
        verify(postTagRepository).batchInsertPostTags(POST_ID, Set.of(testTag));
    }

    @Test
    void testUpdatePostThrowsExceptionWhenValidationFails() {
        UpdatePostDto invalidDto = new UpdatePostDto(1L, null, null, null);

        assertThatThrownBy(() -> postService.updatePost(POST_ID, invalidDto))
                .isInstanceOf(MandatoryParameterAbsentException.class);

        verify(postRepository, never()).update(anyLong(), any());
    }

    @Test
    void testUpdatePostDeletesOldTagsFirst() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(postRepository.update(eq(POST_ID), any(Post.class))).thenReturn(testPost);
        when(tagRepository.saveTags(anyLong(), any())).thenReturn(Set.of());

        postService.updatePost(POST_ID, updatePostDto);

        verify(postTagRepository).deleteByPostId(POST_ID);
    }

    @Test
    void testDeletePostByIdSuccessfully() {
        postService.deletePostById(POST_ID);

        verify(postRepository).deleteById(POST_ID);
        verify(postTagRepository).deleteByPostId(POST_ID);
        verify(commentRepository).deleteAllByPostId(POST_ID);
    }

    @Test
    void testDeletePostByIdCallsAllRepositories() {
        postService.deletePostById(POST_ID);

        verify(postRepository, times(1)).deleteById(POST_ID);
        verify(postTagRepository, times(1)).deleteByPostId(POST_ID);
        verify(commentRepository, times(1)).deleteAllByPostId(POST_ID);
    }

    @Test
    void testIncrementAndGetLikesCount() {
        int expectedCount = 5;
        when(postRepository.incrementAndGetLikesCount(POST_ID)).thenReturn(expectedCount);

        int result = postService.incrementAndGetLikesCount(POST_ID);

        assertThat(result).isEqualTo(expectedCount);
        verify(postRepository).incrementAndGetLikesCount(POST_ID);
    }

    @Test
    void testSaveAndGetPost() {
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(tagRepository.saveTags(anyLong(), any())).thenReturn(Set.of(testTag));
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of(testTag));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        postService.savePost(createPostDto);
        Post result = postService.getPostById(POST_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTags()).contains(testTag);
    }

    @Test
    void testUpdateAndGetPost() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(postRepository.update(eq(POST_ID), any(Post.class))).thenReturn(testPost);
        when(tagRepository.saveTags(anyLong(), any())).thenReturn(Set.of(testTag));
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of(testTag));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        postService.updatePost(POST_ID, updatePostDto);
        Post result = postService.getPostById(POST_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void testDeleteAndGetPost() {
        postService.deletePostById(POST_ID);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(POST_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(postRepository).deleteById(POST_ID);
    }

    @Test
    void testGetPostsWithSearch() {
        Page<Post> postPage = new PageImpl<>(List.of(testPost), PageRequest.of(0, 10), 1);

        when(postRepository.findAllByTextContainsIgnoreCase(eq("search"), any(PageRequest.class)))
                .thenReturn(postPage);
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of());
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        PostPageResponse response = postService.getPosts("search", 1, 10);

        assertThat(response.posts()).hasSize(1);
        verify(postRepository).findAllByTextContainsIgnoreCase(eq("search"), any());
    }

    @Test
    void testGetPostsSortingByIdDescending() {
        Page<Post> postPage = new PageImpl<>(List.of(testPost), PageRequest.of(0, 10), 1);

        when(postRepository.findAllByTextContainsIgnoreCase(eq(""), any(PageRequest.class)))
                .thenReturn(postPage);
        when(tagRepository.findTagsByPostId(POST_ID)).thenReturn(Set.of());
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        postService.getPosts("", 1, 10);

        verify(postRepository).findAllByTextContainsIgnoreCase(eq(""), argThat(pr ->
                pr.getSort().getOrderFor("id") != null &&
                pr.getSort().getOrderFor("id").isDescending()
        ));
    }
}
