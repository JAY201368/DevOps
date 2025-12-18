package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.CommentPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.vo.CommentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private CommentPO testComment;
    private UserPO testUser;
    private ProductPO testProduct;

    @BeforeEach
    void setUp() {
        testUser = new UserPO();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test User");

        testProduct = new ProductPO();
        testProduct.setId(1L);
        testProduct.setTitle("Test Product");
        testProduct.setRate(4.0);

        testComment = new CommentPO();
        testComment.setId(1L);
        testComment.setUserId(1L);
        testComment.setProductId(1L);
        testComment.setContent("Great product!");
        testComment.setRating(5.0);
        testComment.setStatus(1);
        testComment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetCommentsByProductId_Success() {
        Page<CommentPO> commentsPage = new PageImpl<>(Arrays.asList(testComment));
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(commentsPage);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        var result = commentService.getCommentsByProductId(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great product!", result.get(0).getContent());
        assertEquals("testuser", result.get(0).getUsername());
        verify(commentRepository, times(1)).findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class));
    }

    @Test
    void testGetCommentsByProductId_WithDefaultPagination() {
        Page<CommentPO> commentsPage = new PageImpl<>(Arrays.asList(testComment));
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(commentsPage);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        var result = commentService.getCommentsByProductId(1L, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetCommentsByProductId_EmptyResults() {
        Page<CommentPO> emptyPage = new PageImpl<>(Arrays.asList());
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(emptyPage);

        var result = commentService.getCommentsByProductId(1L, 1, 10);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testAddComment_Success() {
        when(commentRepository.save(any(CommentPO.class))).thenAnswer(invocation -> {
            CommentPO comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setCreatedAt(LocalDateTime.now());
            return comment;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(testComment)));

        CommentVO result = commentService.addComment(1L, 1L, "Great product!", 5.0);

        assertNotNull(result);
        assertEquals("Great product!", result.getContent());
        assertEquals(5.0, result.getRating());
        assertEquals("testuser", result.getUsername());
        verify(commentRepository, times(1)).save(any(CommentPO.class));
        verify(productRepository, times(1)).save(any(ProductPO.class));
    }

    @Test
    void testAddComment_NullContent() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.addComment(1L, 1L, null, 5.0);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddComment_EmptyContent() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.addComment(1L, 1L, "   ", 5.0);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddComment_ContentTooLong() {
        String longContent = "a".repeat(501);
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.addComment(1L, 1L, longContent, 5.0);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddComment_NullRating() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.addComment(1L, 1L, "Great product!", null);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddComment_RatingTooLow() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.addComment(1L, 1L, "Great product!", -1.0);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddComment_RatingTooHigh() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.addComment(1L, 1L, "Great product!", 6.0);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddComment_ValidRatingBoundaries() {
        when(commentRepository.save(any(CommentPO.class))).thenAnswer(invocation -> {
            CommentPO comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setCreatedAt(LocalDateTime.now());
            return comment;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        // Test rating 0
        CommentVO result1 = commentService.addComment(1L, 1L, "Bad product", 0.0);
        assertNotNull(result1);
        assertEquals(0.0, result1.getRating());

        // Test rating 5
        CommentVO result2 = commentService.addComment(1L, 1L, "Excellent product", 5.0);
        assertNotNull(result2);
        assertEquals(5.0, result2.getRating());
    }

    @Test
    void testDeleteComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(CommentPO.class))).thenReturn(testComment);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        commentService.deleteComment(1L, 1L);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(any(CommentPO.class));
        verify(productRepository, times(1)).save(any(ProductPO.class));
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.deleteComment(999L, 1L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testDeleteComment_UnauthorizedUser() {
        testComment.setUserId(2L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.deleteComment(1L, 1L);
        });
        assertEquals(403, exception.getCode());
    }

    @Test
    void testUpdateProductRating_WithComments() {
        CommentPO comment1 = new CommentPO();
        comment1.setRating(4.0);
        CommentPO comment2 = new CommentPO();
        comment2.setRating(5.0);

        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(comment1, comment2)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductPO.class))).thenReturn(testProduct);
        when(commentRepository.save(any(CommentPO.class))).thenAnswer(invocation -> {
            CommentPO comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setCreatedAt(LocalDateTime.now());
            return comment;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        commentService.addComment(1L, 1L, "Test comment", 5.0);

        verify(productRepository, times(1)).save(any(ProductPO.class));
    }

    @Test
    void testUpdateProductRating_NoComments() {
        when(commentRepository.findByProductIdAndStatus(eq(1L), eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductPO.class))).thenReturn(testProduct);
        when(commentRepository.save(any(CommentPO.class))).thenAnswer(invocation -> {
            CommentPO comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setCreatedAt(LocalDateTime.now());
            return comment;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        commentService.addComment(1L, 1L, "First comment", 5.0);

        verify(productRepository, times(1)).save(any(ProductPO.class));
    }

    @Test
    void testCheckUserPurchaseStatus_Success() {
        when(commentRepository.hasUserPurchasedProduct(1L, 1L)).thenReturn(true);

        boolean result = commentService.checkUserPurchaseStatus(1L, 1L);

        assertTrue(result);
        verify(commentRepository, times(1)).hasUserPurchasedProduct(1L, 1L);
    }

    @Test
    void testCheckUserPurchaseStatus_NotPurchased() {
        when(commentRepository.hasUserPurchasedProduct(1L, 1L)).thenReturn(false);

        boolean result = commentService.checkUserPurchaseStatus(1L, 1L);

        assertFalse(result);
    }

    @Test
    void testCheckUserPurchaseStatus_Exception() {
        when(commentRepository.hasUserPurchasedProduct(1L, 1L))
                .thenThrow(new RuntimeException("Database error"));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            commentService.checkUserPurchaseStatus(1L, 1L);
        });
        assertEquals(500, exception.getCode());
    }
}
