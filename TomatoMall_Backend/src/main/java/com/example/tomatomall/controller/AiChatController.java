package com.example.tomatomall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.CommentPO;
import com.example.tomatomall.po.CouponPO;
import com.example.tomatomall.po.SpecificationPO;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.repository.CouponRepository;
import com.example.tomatomall.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai-chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final ProductRepository productRepository;
    private final CommentRepository commentRepository;
    private final CouponRepository couponRepository;

    public AiChatController(ProductRepository productRepository, CommentRepository commentRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.commentRepository = commentRepository;
        this.couponRepository = couponRepository;
    }

    @GetMapping("/context")
    public ResultVO<String> getChatContext() {
        try {
            // 获取所有图书信息
            List<ProductPO> books = productRepository.findAll();
            
            // 获取所有评论
            List<CommentPO> comments = commentRepository.findAll();
            
            // 获取所有优惠券
            List<CouponPO> coupons = couponRepository.findAll();
            
            // 构建图书上下文信息
            StringBuilder context = new StringBuilder();
            context.append("以下是番茄书城的图书信息，可以作为回答用户问题的参考：\n\n");
            
            // 添加图书基本信息
            for (ProductPO book : books) {
                context.append("书名：").append(book.getTitle()).append("\n");
                context.append("描述：").append(book.getDescription()).append("\n");
                context.append("评分：").append(book.getRate() != null ? book.getRate() : "暂无评分").append("\n");
                context.append("价格：").append(book.getPrice()).append("元\n");
                context.append("标签：").append(book.getTags() != null ? book.getTags() : "暂无标签").append("\n");
                
                // 添加图书规格信息
                if (book.getSpecifications() != null && !book.getSpecifications().isEmpty()) {
                    context.append("规格信息：\n");
                    for (SpecificationPO spec : book.getSpecifications()) {
                        context.append("  - ").append(spec.getItem()).append(": ").append(spec.getValue()).append("\n");
                    }
                }
                
                // 添加库存信息
                if (book.getStockpile() != null) {
                    context.append("库存：").append(book.getStockpile().getAmount()).append("本\n");
                }
                
                // 添加该图书的评论信息
                List<CommentPO> bookComments = comments.stream()
                    .filter(c -> c.getProductId().equals(book.getId()))
                    .collect(Collectors.toList());
                
                if (!bookComments.isEmpty()) {
                    context.append("评论信息：\n");
                    for (CommentPO comment : bookComments) {
                        context.append("  - 评分：").append(comment.getRating())
                              .append("，评论：").append(comment.getContent())
                              .append("（").append(comment.getCreatedAt()).append("）\n");
                    }
                }
                
                // 添加相关优惠券信息
                LocalDateTime now = LocalDateTime.now();
                List<CouponPO> bookCoupons = coupons.stream()
                    .filter(c -> c.getStatus() == 1 && 
                           now.isAfter(c.getStartDate()) && 
                           now.isBefore(c.getEndDate()) && 
                           c.getRemainingQuantity() > 0)
                    .collect(Collectors.toList());
                
                if (!bookCoupons.isEmpty()) {
                    context.append("当前优惠：\n");
                    for (CouponPO coupon : bookCoupons) {
                        context.append("  - ").append(coupon.getName())
                              .append("：").append(coupon.getDescription())
                              .append("（优惠金额：").append(coupon.getDiscountAmount())
                              .append("元，最低订单金额：").append(coupon.getMinOrderAmount())
                              .append("元）\n");
                    }
                }
                
                context.append("\n");
            }
            
            // 添加系统提示
            context.append("\n请基于以上图书信息，回答用户的问题。如果用户的问题与图书无关，请礼貌地告知用户您只能回答与图书相关的问题。\n");
            context.append("在回答时，请注意：\n");
            context.append("1. 优先使用图书的基本信息（书名、描述、评分、价格、标签）回答用户问题\n");
            context.append("2. 如果用户询问具体图书的评价，可以参考该图书的评论信息\n");
            context.append("3. 如果用户询问价格或优惠，请告知当前可用的优惠券信息\n");
            context.append("4. 如果用户询问库存，请告知具体的库存数量\n");
            context.append("5. 如果用户询问图书规格，请提供详细的规格信息\n");
            context.append("6. 如果用户的问题涉及多本图书，请综合考虑所有相关信息\n");
            
            return ResultVO.buildSuccess(context.toString());
        } catch (Exception e) {
            log.error("获取图书上下文失败", e);
            return ResultVO.buildFailure("获取图书上下文失败", "500");
        }
    }
} 