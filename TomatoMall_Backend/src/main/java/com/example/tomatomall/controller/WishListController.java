package com.example.tomatomall.controller;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.WishListService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.ResultVO;
import com.example.tomatomall.vo.WishListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WishListController {

    @Autowired
    private WishListService wishListService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // 获取当前登录用户
    private UserPO getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new TomatoMallException(401, "未授权");
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new TomatoMallException(404, "用户不存在"));
    }

    /**
     * 添加图书到愿望单
     */
    @PostMapping
    public ResultVO<WishListItemVO> addToWishList(@RequestBody Map<String, Long> requestBody, HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            Long bookId = requestBody.get("bookId");
            
            WishListItemVO wishListItem = wishListService.addToWishList(currentUser.getId(), bookId);
            return ResultVO.buildSuccess(wishListItem);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), String.valueOf(e.getCode()));
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 获取用户的愿望单列表
     */
    @GetMapping
    public ResultVO<List<WishListItemVO>> getWishList(HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            List<WishListItemVO> wishList = wishListService.getWishList(currentUser.getId());
            return ResultVO.buildSuccess(wishList);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), String.valueOf(e.getCode()));
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 从愿望单中删除图书
     */
    @DeleteMapping("/{bookId}")
    public ResultVO<String> removeFromWishList(@PathVariable Long bookId, HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            wishListService.removeFromWishList(currentUser.getId(), bookId);
            return ResultVO.buildSuccess("成功从愿望单移除");
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), String.valueOf(e.getCode()));
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 检查图书是否在愿望单中
     */
    @GetMapping("/check/{bookId}")
    public ResultVO<Boolean> checkInWishList(@PathVariable Long bookId, HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            boolean isInWishList = wishListService.isInWishList(currentUser.getId(), bookId);
            return ResultVO.buildSuccess(isInWishList);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), String.valueOf(e.getCode()));
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
}
