package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.CommentPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.CommentService;
import com.example.tomatomall.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<CommentVO> getCommentsByProductId(Long productId, Integer page, Integer size) {
        // 设置分页和排序
        PageRequest pageRequest = PageRequest.of(
            page == null ? 0 : page - 1,
            size == null ? 10 : size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 获取评论列表
        Page<CommentPO> commentPage = commentRepository.findByProductIdAndStatus(productId, 1, pageRequest);
        
        // 转换为VO并添加用户名
        return commentPage.getContent().stream().map(comment -> {
            CommentVO vo = CommentVO.fromPO(comment);
            // 获取并设置用户名
            userRepository.findById(comment.getUserId()).ifPresent(user -> 
                vo.setUsername(user.getUsername())
            );
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentVO addComment(Long userId, Long productId, String content, Double rating) {
        // 验证评论内容
        if (content == null || content.trim().isEmpty()) {
            throw new TomatoMallException(400, "评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new TomatoMallException(400, "评论内容不能超过500字");
        }

        // 验证评分
        if (rating == null) {
            throw new TomatoMallException(400, "评分不能为空");
        }
        if (rating < 0 || rating > 5) {
            throw new TomatoMallException(400, "评分必须在0-5分之间");
        }

        // 创建评论
        CommentPO comment = new CommentPO();
        comment.setUserId(userId);
        comment.setProductId(productId);
        comment.setContent(content.trim());
        comment.setRating(rating);
        comment.setStatus(1);

        // 保存评论
        CommentPO savedComment = commentRepository.save(comment);
        CommentVO vo = CommentVO.fromPO(savedComment);
        
        // 设置用户名
        userRepository.findById(userId).ifPresent(user -> 
            vo.setUsername(user.getUsername())
        );

        // 更新商品的平均评分
        updateProductRating(productId);
        
        return vo;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommentPO comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new TomatoMallException(404, "评论不存在"));

        // 验证是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new TomatoMallException(403, "无权删除此评论");
        }

        // 软删除评论
        comment.setStatus(0);
        commentRepository.save(comment);

        // 更新商品的平均评分
        updateProductRating(comment.getProductId());
    }

    // 添加更新商品评分的方法
    private void updateProductRating(Long productId) {
        // 获取商品的所有有效评论
        List<CommentPO> comments = commentRepository.findByProductIdAndStatus(productId, 1, Pageable.unpaged()).getContent();
        
        ProductPO product = productRepository.findById(productId)
            .orElseThrow(() -> new TomatoMallException(404, "商品不存在"));
            
        if (!comments.isEmpty()) {
            // 计算平均评分
            double averageRating = comments.stream()
                .mapToDouble(CommentPO::getRating)
                .average()
                .orElse(0.0);
            
            // 直接使用5分制的评分
            product.setRate(averageRating);
        } else {
            // 如果没有评论，设置评分为null
            product.setRate(null);
        }
        productRepository.save(product);
    }

    @Override
    public boolean checkUserPurchaseStatus(Long userId, Long productId) {
        try {
            return commentRepository.hasUserPurchasedProduct(userId, productId);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误信息
            throw new TomatoMallException(500, "检查购买状态失败：" + e.getMessage());
        }
    }
} 