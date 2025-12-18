package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.po.WishListItemPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.repository.WishListRepository;
import com.example.tomatomall.vo.WishListItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishListServiceImplTest {

    @Mock
    private WishListRepository wishListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WishListServiceImpl wishListService;

    private UserPO testUser;
    private WishListItemPO testWishListItem;

    @BeforeEach
    void setUp() {
        testUser = new UserPO();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test User");

        testWishListItem = new WishListItemPO();
        testWishListItem.setId(1L);
        testWishListItem.setUser(testUser);
        testWishListItem.setBookId(1L);
    }

    @Test
    void testAddToWishList_Success() {
        when(wishListRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(wishListRepository.save(any(WishListItemPO.class))).thenAnswer(invocation -> {
            WishListItemPO item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        WishListItemVO result = wishListService.addToWishList(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        verify(wishListRepository, times(1)).existsByUserIdAndBookId(1L, 1L);
        verify(wishListRepository, times(1)).save(any(WishListItemPO.class));
    }

    @Test
    void testAddToWishList_AlreadyExists() {
        when(wishListRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            wishListService.addToWishList(1L, 1L);
        });
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已在您的愿望单中"));
    }

    @Test
    void testAddToWishList_UserNotFound() {
        when(wishListRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            wishListService.addToWishList(1L, 1L);
        });
        assertEquals(404, exception.getCode());
        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void testGetWishList_Success() {
        WishListItemPO item1 = new WishListItemPO();
        item1.setId(1L);
        item1.setUser(testUser);
        item1.setBookId(1L);

        WishListItemPO item2 = new WishListItemPO();
        item2.setId(2L);
        item2.setUser(testUser);
        item2.setBookId(2L);

        when(wishListRepository.findByUserId(1L)).thenReturn(Arrays.asList(item1, item2));

        var result = wishListService.getWishList(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getBookId());
        assertEquals(2L, result.get(1).getBookId());
        verify(wishListRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetWishList_EmptyList() {
        when(wishListRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        var result = wishListService.getWishList(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testRemoveFromWishList_Success() {
        when(wishListRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testWishListItem));
        doNothing().when(wishListRepository).delete(any(WishListItemPO.class));

        wishListService.removeFromWishList(1L, 1L);

        verify(wishListRepository, times(1)).findByUserIdAndBookId(1L, 1L);
        verify(wishListRepository, times(1)).delete(testWishListItem);
    }

    @Test
    void testRemoveFromWishList_NotFound() {
        when(wishListRepository.findByUserIdAndBookId(1L, 999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            wishListService.removeFromWishList(1L, 999L);
        });
        assertEquals(404, exception.getCode());
        assertTrue(exception.getMessage().contains("不在愿望单中"));
    }

    @Test
    void testIsInWishList_True() {
        when(wishListRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        boolean result = wishListService.isInWishList(1L, 1L);

        assertTrue(result);
        verify(wishListRepository, times(1)).existsByUserIdAndBookId(1L, 1L);
    }

    @Test
    void testIsInWishList_False() {
        when(wishListRepository.existsByUserIdAndBookId(1L, 999L)).thenReturn(false);

        boolean result = wishListService.isInWishList(1L, 999L);

        assertFalse(result);
        verify(wishListRepository, times(1)).existsByUserIdAndBookId(1L, 999L);
    }

    @Test
    void testAddToWishList_MultipleItems() {
        when(wishListRepository.existsByUserIdAndBookId(eq(1L), anyLong())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(wishListRepository.save(any(WishListItemPO.class))).thenAnswer(invocation -> {
            WishListItemPO item = invocation.getArgument(0);
            item.setId(System.currentTimeMillis());
            return item;
        });

        WishListItemVO result1 = wishListService.addToWishList(1L, 1L);
        WishListItemVO result2 = wishListService.addToWishList(1L, 2L);
        WishListItemVO result3 = wishListService.addToWishList(1L, 3L);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(1L, result1.getBookId());
        assertEquals(2L, result2.getBookId());
        assertEquals(3L, result3.getBookId());
        verify(wishListRepository, times(3)).save(any(WishListItemPO.class));
    }

    @Test
    void testRemoveFromWishList_MultipleTimes() {
        when(wishListRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testWishListItem));
        when(wishListRepository.findByUserIdAndBookId(1L, 2L)).thenReturn(Optional.empty());
        doNothing().when(wishListRepository).delete(any(WishListItemPO.class));

        // First removal should succeed
        wishListService.removeFromWishList(1L, 1L);

        // Second removal should fail
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            wishListService.removeFromWishList(1L, 2L);
        });
        assertEquals(404, exception.getCode());

        verify(wishListRepository, times(1)).delete(testWishListItem);
    }
}
