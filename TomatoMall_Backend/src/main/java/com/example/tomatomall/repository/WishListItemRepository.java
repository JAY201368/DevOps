package com.example.tomatomall.repository;

import com.example.tomatomall.po.WishListItemPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 愿望单项目数据访问接口
 * 
 * 提供愿望单相关的数据库操作，包括：
 * 1. 查询用户的愿望单项目
 * 2. 检查特定图书是否在用户愿望单中
 * 3. 删除愿望单项目
 * 4. 继承JpaRepository的基础CRUD操作
 * 
 * 主要用于推荐算法中分析用户偏好，
 * 基于愿望单内容推荐相似书籍。
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Repository
public interface WishListItemRepository extends JpaRepository<WishListItemPO, Long> {
    
    /**
     * 查找指定用户的所有愿望单项目
     * 
     * 查询用户添加到愿望单的所有图书项目。
     * 用于推荐算法中分析用户的兴趣偏好，
     * 基于愿望单中的书籍标签推荐相似书籍。
     * 
     * @param userId 用户ID，不能为null
     * @return 用户愿望单项目列表，如果用户没有愿望单则返回空列表
     */
    List<WishListItemPO> findByUserId(Long userId);
    
    /**
     * 查找指定用户的指定图书的愿望单项目
     * 
     * 检查特定图书是否已存在于用户的愿望单中。
     * 用于前端判断是否显示"添加到愿望单"或"从愿望单移除"按钮。
     * 
     * @param userId 用户ID，不能为null
     * @param bookId 图书ID，不能为null
     * @return 愿望单项目，如果不存在则返回空的Optional
     */
    Optional<WishListItemPO> findByUserIdAndBookId(Long userId, Long bookId);
    
    /**
     * 删除指定用户的指定图书的愿望单项目
     * 
     * 从用户愿望单中移除指定的图书。
     * 用于用户取消愿望单中的图书项目。
     * 
     * @param userId 用户ID，不能为null
     * @param bookId 图书ID，不能为null
     */
    void deleteByUserIdAndBookId(Long userId, Long bookId);
} 