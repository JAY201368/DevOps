package com.example.tomatomall.controller;

import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.service.RecommendationService;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.ResultVO;
import com.example.tomatomall.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

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
            // 限制最多返回9个热门推荐商品
            int actualLimit = Math.min(limit, 9);
            List<ProductVO> popularBooks = recommendationService.getPopularRecommendations(actualLimit);
            return ResultVO.buildSuccess(popularBooks);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误堆栈
            return ResultVO.buildFailure("获取热门推荐失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 基于用户愿望单获取推荐书籍
     */
    @GetMapping("/wishlist")
    public ResultVO<List<ProductVO>> getWishListBasedRecommendations(
            @RequestParam(defaultValue = "9") int limit,
            HttpServletRequest request) {
        try {
            // 获取当前登录用户
            UserPO currentUser = getCurrentUser(request);
            if (currentUser == null) {
                // 未登录用户返回空列表
                System.out.println("用户未登录，返回空列表");
                return ResultVO.buildSuccess(new ArrayList<>());
            }
            
            // 获取基于愿望单的推荐
            System.out.println("为用户ID: " + currentUser.getId() + " 获取基于愿望单的推荐，限制数量: " + limit);
            List<ProductVO> recommendations = recommendationService.getWishListBasedRecommendations(currentUser.getId(), limit);
            System.out.println("成功获取到 " + recommendations.size() + " 本基于愿望单的推荐书籍");
            
            // 如果没有获取到推荐，记录日志
            if (recommendations.isEmpty()) {
                System.out.println("没有找到匹配用户愿望单标签的推荐书籍");
            }
            
            return ResultVO.buildSuccess(recommendations);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误堆栈
            return ResultVO.buildFailure("获取推荐失败: " + e.getMessage(), "500");
        }
    }
    
    /**
     * 从请求中获取当前登录用户
     */
    private UserPO getCurrentUser(HttpServletRequest request) {
        // 从请求头中获取token (检查两种可能的header名称)
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            // 如果token头为空，尝试从Authorization头获取
            token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                System.out.println("未找到认证令牌，token和Authorization头均为空");
            return null;
        }
        }
        
        try {
            // 验证token有效性
            if (!jwtUtil.validateToken(token)) {
                System.out.println("令牌验证失败，可能已过期或无效");
                return null;
            }
            
            // 从token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null || username.isEmpty()) {
                System.out.println("无法从令牌中提取用户名");
                return null;
            }
            
            // 获取用户信息
            UserVO userVO = userService.getUserByUsername(username);
            if (userVO == null) {
                System.out.println("未找到用户: " + username);
                return null;
            }
            
            // 构造UserPO对象
            UserPO userPO = new UserPO();
            userPO.setId(userVO.getId());
            userPO.setUsername(userVO.getUsername());
            userPO.setRole(userVO.getRole());
            
            return userPO;
        } catch (Exception e) {
            System.out.println("处理认证令牌时出错: " + e.getMessage());
            return null;
        }
    }
} 