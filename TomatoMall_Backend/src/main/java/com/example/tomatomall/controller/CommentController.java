package com.example.tomatomall.controller;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.service.CommentService;
import com.example.tomatomall.vo.CommentVO;
import com.example.tomatomall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取商品评论列表
     * GET /api/comments/list
     */
    @GetMapping("/list")
    public ResultVO<List<CommentVO>> getComments(
            @RequestParam Long productId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<CommentVO> comments = commentService.getCommentsByProductId(productId, page, size);
            return ResultVO.buildSuccess(comments);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 添加评论
     * POST /api/comments/add
     */
    @PostMapping("/add")
    public ResultVO<CommentVO> addComment(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam String content,
            @RequestParam Double rating) {
        try {
            CommentVO comment = commentService.addComment(userId, productId, content, rating);
            return ResultVO.buildSuccess(comment);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 删除评论
     * DELETE /api/comments/delete/{commentId}
     */
    @DeleteMapping("/delete/{commentId}")
    public ResultVO<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        try {
            commentService.deleteComment(commentId, userId);
            return ResultVO.buildSuccess(null);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 检查用户购买状态
     * GET /api/comments/check-purchase
     */
    @GetMapping("/check-purchase")
    public ResultVO<Boolean> checkPurchaseStatus(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        try {
            boolean hasPurchased = commentService.checkUserPurchaseStatus(userId, productId);
            return ResultVO.buildSuccess(hasPurchased);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
}