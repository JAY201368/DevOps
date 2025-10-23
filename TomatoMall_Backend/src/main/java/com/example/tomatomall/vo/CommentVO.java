package com.example.tomatomall.vo;

import com.example.tomatomall.po.CommentPO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private String username;  // 评论者用户名
    private Long productId;
    private String content;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer status;

    // 从PO转换为VO
    public static CommentVO fromPO(CommentPO po) {
        CommentVO vo = new CommentVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setProductId(po.getProductId());
        vo.setContent(po.getContent());
        vo.setRating(po.getRating());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        vo.setStatus(po.getStatus());
        return vo;
    }
} 