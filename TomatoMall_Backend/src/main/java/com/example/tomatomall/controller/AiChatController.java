package com.example.tomatomall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Map;

import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.CommentPO;
import com.example.tomatomall.po.CouponPO;
import com.example.tomatomall.po.SpecificationPO;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.repository.CouponRepository;
import com.example.tomatomall.vo.ResultVO;
import com.example.tomatomall.service.AiChatService;
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

    @Autowired
    private AiChatService aiChatService;

    public AiChatController(ProductRepository productRepository, CommentRepository commentRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.commentRepository = commentRepository;
        this.couponRepository = couponRepository;
    }

    @GetMapping("/context")
    public ResultVO<String> getChatContext(@RequestParam(required = false) String userId) {
        try {
            // 获取图书上下文
            String bookContext = aiChatService.getChatContext(null);
            
            // 如果提供了用户ID，添加对话历史
            if (userId != null && !userId.isEmpty()) {
                String conversationContext = aiChatService.getConversationContext(userId);
                bookContext += conversationContext;
            }
            
            return ResultVO.buildSuccess(bookContext);
        } catch (Exception e) {
            log.error("获取聊天上下文失败", e);
            return ResultVO.buildFailure("获取聊天上下文失败", "500");
        }
    }

    @PostMapping("/conversation")
    public ResultVO<Void> addConversation(
            @RequestParam String userId,
            @RequestParam String question,
            @RequestParam String answer) {
        try {
            aiChatService.addConversation(userId, question, answer);
            return ResultVO.buildSuccess(null);
        } catch (Exception e) {
            log.error("添加对话历史失败", e);
            return ResultVO.buildFailure("添加对话历史失败", "500");
        }
    }

    @GetMapping("/conversation/history")
    public ResultVO<List<Map<String, String>>> getConversationHistory(@RequestParam String userId) {
        try {
            List<Map<String, String>> history = aiChatService.getConversationHistory(userId);
            return ResultVO.buildSuccess(history);
        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return ResultVO.buildFailure("获取对话历史失败", "500");
        }
    }

    @DeleteMapping("/conversation/history")
    public ResultVO<Void> clearConversationHistory(@RequestParam String userId) {
        try {
            aiChatService.clearConversationHistory(userId);
            return ResultVO.buildSuccess(null);
        } catch (Exception e) {
            log.error("清除对话历史失败", e);
            return ResultVO.buildFailure("清除对话历史失败", "500");
        }
    }
} 