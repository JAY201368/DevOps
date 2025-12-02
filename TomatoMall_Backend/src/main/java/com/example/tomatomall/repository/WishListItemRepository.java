package com.example.tomatomall.repository;

import com.example.tomatomall.po.WishListItemPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListItemRepository extends JpaRepository<WishListItemPO, Long> {
    
    /**
     * 查找指定用户的所有愿望单项目
     * @param userId 用户ID
     * @return 愿望单项目列表
     */
    List<WishListItemPO> findByUserId(Long userId);
    
    /**
     * 查找指定用户的指定图书的愿望单项目
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 愿望单项目（如果存在）
     */
    Optional<WishListItemPO> findByUserIdAndBookId(Long userId, Long bookId);
    
    /**
     * 删除指定用户的指定图书的愿望单项目
     * @param userId 用户ID
     * @param bookId 图书ID
     */
    void deleteByUserIdAndBookId(Long userId, Long bookId);
} 