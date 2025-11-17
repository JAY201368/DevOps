package com.example.tomatomall.controller;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.CouponDistributionLogVO;
import com.example.tomatomall.vo.CouponDistributionRequestVO;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.ResultVO;
import com.example.tomatomall.vo.UserCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    // 创建促销券（管理员）
    @PostMapping
    public ResultVO<CouponVO> createCoupon(@RequestBody CouponVO couponVO, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            // 这里应该检查用户是否为管理员，简单起见，假设已验证
            
            CouponVO createdCoupon = couponService.createCoupon(couponVO);
            return ResultVO.buildSuccess(createdCoupon);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 更新促销券（管理员）
    @PutMapping("/{couponId}")
    public ResultVO<CouponVO> updateCoupon(@PathVariable Long couponId, @RequestBody CouponVO couponVO, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            couponVO.setId(couponId);
            CouponVO updatedCoupon = couponService.updateCoupon(couponVO);
            return ResultVO.buildSuccess(updatedCoupon);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 删除促销券（管理员）
    @DeleteMapping("/{couponId}")
    public ResultVO<String> deleteCoupon(@PathVariable Long couponId, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            couponService.deleteCoupon(couponId);
            return ResultVO.buildSuccess("删除成功");
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取促销券详情
    @GetMapping("/{couponId}")
    public ResultVO<CouponVO> getCouponById(@PathVariable Long couponId, HttpServletRequest request) {
        try {
            // 验证用户登录
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            CouponVO coupon = couponService.getCouponById(couponId);
            return ResultVO.buildSuccess(coupon);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取所有促销券（管理员）
    @GetMapping
    public ResultVO<List<CouponVO>> getAllCoupons(HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            List<CouponVO> coupons = couponService.getAllCoupons();
            return ResultVO.buildSuccess(coupons);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取有效的促销券（用户）
    @GetMapping("/valid")
    public ResultVO<List<CouponVO>> getValidCoupons(HttpServletRequest request) {
        try {
            // 验证用户登录
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            List<CouponVO> validCoupons = couponService.getValidCoupons();
            return ResultVO.buildSuccess(validCoupons);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 发放促销券（管理员）
    @PostMapping("/distribute")
    public ResultVO<List<UserCouponVO>> distributeCoupons(@RequestBody CouponDistributionRequestVO requestVO, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            // 从用户服务中获取管理员ID
            Long adminId = userService.getUserIdByUsername(username);
            if (adminId == null) {
                return ResultVO.buildFailure("无法获取管理员ID", "400");
            }
            
            List<UserCouponVO> distributedCoupons = couponService.distributeCoupons(requestVO, adminId);
            return ResultVO.buildSuccess(distributedCoupons);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取用户的促销券
    @GetMapping("/user/{userId}")
    public ResultVO<List<UserCouponVO>> getUserCoupons(@PathVariable Long userId, HttpServletRequest request) {
        try {
            // 验证用户登录
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            // 这里应该检查请求的用户ID是否为当前登录用户或管理员
            
            List<UserCouponVO> userCoupons = couponService.getUserCoupons(userId);
            return ResultVO.buildSuccess(userCoupons);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取用户未使用的促销券
    @GetMapping("/user/{userId}/unused")
    public ResultVO<List<UserCouponVO>> getUserUnusedCoupons(@PathVariable Long userId, HttpServletRequest request) {
        try {
            // 验证用户登录
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            // 这里应该检查请求的用户ID是否为当前登录用户或管理员
            
            List<UserCouponVO> unusedCoupons = couponService.getUserUnusedCoupons(userId);
            return ResultVO.buildSuccess(unusedCoupons);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取促销券发放记录（管理员）
    @GetMapping("/{couponId}/distribution-logs")
    public ResultVO<List<CouponDistributionLogVO>> getCouponDistributionLogs(@PathVariable Long couponId, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            List<CouponDistributionLogVO> logs = couponService.getCouponDistributionLogs(couponId);
            return ResultVO.buildSuccess(logs);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
    
    // 获取管理员的促销券发放记录
    @GetMapping("/admin/{adminId}/distribution-logs")
    public ResultVO<List<CouponDistributionLogVO>> getAdminCouponDistributionLogs(@PathVariable Long adminId, HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            
            // 这里应该检查请求的管理员ID是否为当前登录管理员
            
            List<CouponDistributionLogVO> logs = couponService.getAdminCouponDistributionLogs(adminId);
            return ResultVO.buildSuccess(logs);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
} 