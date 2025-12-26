package com.nesterukia.blog.unit.service;

import com.nesterukia.blog.dto.comment.CreateCommentDto;
import com.nesterukia.blog.exceptions.EntityNotFoundException;
import com.nesterukia.blog.exceptions.MandatoryParameterAbsentException;
import com.nesterukia.blog.model.Comment;
import com.nesterukia.blog.model.Post;
import com.nesterukia.blog.repository.CommentRepository;
import com.nesterukia.blog.repository.PostRepository;
import com.nesterukia.blog.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private CreateCommentDto createCommentDto;
    private Comment testComment;
    private Post testPost;
    private final Long POST_ID = 1L;
    private final Long COMMENT_ID = 100L;

    @BeforeEach
    void setUp() {
        testPost = Post.builder()
                .id(POST_ID)
                .title("Test Post")
                .build();

        testComment = Comment.builder()
                .id(COMMENT_ID)
                .postId(POST_ID)
                .text("Test Comment")
                .build();

        createCommentDto = new CreateCommentDto("New Comment", POST_ID);
    }

    @Test
    void testSaveCommentSuccessfully() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment result = commentService.save(createCommentDto);

        assertThat(result).isEqualTo(testComment);
        verify(postRepository).findById(POST_ID);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testSaveThrowsExceptionWhenPostNotFound() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.save(createCommentDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No Post found");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void testSaveThrowsExceptionWhenValidationFails() {
        CreateCommentDto invalidDto = new CreateCommentDto(null, null);

        assertThatThrownBy(() -> commentService.save(invalidDto))
                .isInstanceOf(MandatoryParameterAbsentException.class);

        verify(postRepository, never()).findById(any());
    }

    @Test
    void testUpdateCommentSuccessfully() {
        String updatedText = "Updated text";
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.update(POST_ID, COMMENT_ID, updatedText))
                .thenReturn(testComment.withText(updatedText));

        Comment result = commentService.updateComment(POST_ID, COMMENT_ID, updatedText);

        assertThat(result.getText()).isEqualTo(updatedText);
        verify(postRepository).findById(POST_ID);
        verify(commentRepository).update(POST_ID, COMMENT_ID, updatedText);
    }

    @Test
    void testUpdateThrowsExceptionWhenPostNotFound() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateComment(POST_ID, COMMENT_ID, "text"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commentRepository, never()).update(any(), any(), any());
    }

    @Test
    void testDeleteCommentSuccessfully() {
        commentService.delete(POST_ID, COMMENT_ID);

        verify(commentRepository).delete(POST_ID, COMMENT_ID);
    }

    @Test
    void testDeleteDoesNotValidatePost() {
        commentService.delete(POST_ID, COMMENT_ID);

        verify(postRepository, never()).findById(any());
    }

    @Test
    void testFindAllByPostIdSuccessfully() {
        Set<Comment> comments = Set.of(testComment);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(comments);

        Set<Comment> result = commentService.findAllByPostId(POST_ID);

        assertThat(result).hasSize(1).contains(testComment);
        verify(postRepository).findById(POST_ID);
    }

    @Test
    void testFindAllByPostIdReturnsEmpty() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        Set<Comment> result = commentService.findAllByPostId(POST_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindAllThrowsExceptionWhenPostNotFound() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findAllByPostId(POST_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commentRepository, never()).findAllByPostId(any());
    }

    @Test
    void testFindCommentByIdSuccessfully() {
        when(commentRepository.findById(POST_ID, COMMENT_ID)).thenReturn(Optional.of(testComment));

        Comment result = commentService.findCommentById(POST_ID, COMMENT_ID);

        assertThat(result).isEqualTo(testComment);
        verify(commentRepository).findById(POST_ID, COMMENT_ID);
    }

    @Test
    void testFindCommentByIdThrowsException() {
        when(commentRepository.findById(POST_ID, COMMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findCommentById(POST_ID, COMMENT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id = '100'");

        verify(commentRepository).findById(POST_ID, COMMENT_ID);
    }

    @Test
    @DisplayName("✅ findCommentById - не проверяет существование поста")
    void testFindCommentByIdDoesNotValidatePost() {
        when(commentRepository.findById(POST_ID, COMMENT_ID)).thenReturn(Optional.of(testComment));

        commentService.findCommentById(POST_ID, COMMENT_ID);

        verify(postRepository, never()).findById(any());
    }

    // ===== Интеграционные тесты =====
    @Test
    @DisplayName("✅ save -> findAllByPostId - комментарий появляется в списке")
    void testSaveAndFindAll() {
        Set<Comment> comments = Set.of(testComment);
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any())).thenReturn(testComment);
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(comments);

        commentService.save(createCommentDto);
        Set<Comment> result = commentService.findAllByPostId(POST_ID);

        assertThat(result).contains(testComment);
    }

    @Test
    @DisplayName("✅ updateComment -> findCommentById - обновленный комментарий")
    void testUpdateAndFind() {
        String updatedText = "Updated";
        Comment updated = testComment.withText(updatedText);

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.update(POST_ID, COMMENT_ID, updatedText)).thenReturn(updated);
        when(commentRepository.findById(POST_ID, COMMENT_ID)).thenReturn(Optional.of(updated));

        commentService.updateComment(POST_ID, COMMENT_ID, updatedText);
        Comment result = commentService.findCommentById(POST_ID, COMMENT_ID);

        assertThat(result.getText()).isEqualTo(updatedText);
    }

    @Test
    void testDeleteAndFindAll() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.findAllByPostId(POST_ID)).thenReturn(Set.of());

        commentService.delete(POST_ID, COMMENT_ID);
        Set<Comment> result = commentService.findAllByPostId(POST_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void testSaveWithNullText() {
        CreateCommentDto nullTextDto = new CreateCommentDto(null, POST_ID);

        assertThatThrownBy(() -> commentService.save(nullTextDto))
                .isInstanceOf(MandatoryParameterAbsentException.class);
    }

    @Test
    @DisplayName("✅ updateComment с пустым текстом - успешно обновляет")
    void testUpdateWithEmptyText() {
        String emptyText = "";
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.update(POST_ID, COMMENT_ID, emptyText))
                .thenReturn(testComment.withText(emptyText));

        Comment result = commentService.updateComment(POST_ID, COMMENT_ID, emptyText);

        assertThat(result.getText()).isEmpty();
    }
}
