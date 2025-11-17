package com.example.tomatomall.service;

import com.example.tomatomall.vo.WishListItemVO;
import java.util.List;

public interface WishListService {
    WishListItemVO addToWishList(Long userId, Long bookId);
    List<WishListItemVO> getWishList(Long userId);
    void removeFromWishList(Long userId, Long bookId);
    boolean isInWishList(Long userId, Long bookId);
}
