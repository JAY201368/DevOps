package com.example.tomatomall.service;

import com.example.tomatomall.vo.CommentVO;
import java.util.List;

public interface CommentService {
    // 获取商品评论列表
    List<CommentVO> getCommentsByProductId(Long productId, Integer page, Integer size);
    
    // 添加评论
    CommentVO addComment(Long userId, Long productId, String content, Double rating);
    
    // 删除评论
    void deleteComment(Long commentId, Long userId);
    
    // 检查用户是否已购买商品
    boolean checkUserPurchaseStatus(Long userId, Long productId);
} 