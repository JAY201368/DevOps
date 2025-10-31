package com.example.tomatomall.controller;

import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.service.RecommendationService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取个性化推荐书籍
     */
    @GetMapping("/personalized")
    public ResultVO<List<ProductVO>> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "9") int limit,
            HttpServletRequest request) {
        try {
            // 获取当前登录用户
            UserPO currentUser = getCurrentUser(request);
            if (currentUser == null) {
                // 未登录用户返回热门推荐
                System.out.println("用户未登录，返回热门推荐");
                List<ProductVO> popularBooks = recommendationService.getPopularRecommendations(limit);
                return ResultVO.buildSuccess(popularBooks);
            }
            
            // 获取个性化推荐
            System.out.println("为用户ID: " + currentUser.getId() + " 获取个性化推荐");
            List<ProductVO> recommendations = recommendationService.getPersonalizedRecommendations(currentUser.getId(), limit);
            return ResultVO.buildSuccess(recommendations);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误堆栈
            return ResultVO.buildFailure("获取推荐失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 获取热门推荐书籍
     */
    @GetMapping("/popular")
    public ResultVO<List<ProductVO>> getPopularRecommendations(
            @RequestParam(defaultValue = "9") int limit) {
        try {
            System.out.println("获取热门推荐，数量限制: " + limit);
            List<ProductVO> popularBooks = recommendationService.getPopularRecommendations(limit);
            System.out.println("成功获取热门推荐，数量: " + popularBooks.size());
            return ResultVO.buildSuccess(popularBooks);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误堆栈
            return ResultVO.buildFailure("获取热门推荐失败: " + e.getMessage(), "500");
        }
    }
    
    /**
     * 从请求中获取当前登录用户
     */
    private UserPO getCurrentUser(HttpServletRequest request) {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // 从请求属性中获取用户信息
        UserPO currentUser = (UserPO) request.getAttribute("currentUser");
        
        // 如果未找到用户，记录日志但不抛出异常
        if (currentUser == null) {
            System.out.println("未找到当前登录用户，将返回热门推荐");
        }
        
        return currentUser;
    }
} 