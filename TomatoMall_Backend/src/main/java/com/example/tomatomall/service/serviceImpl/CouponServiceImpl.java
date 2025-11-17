package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.CouponDistributionLogPO;
import com.example.tomatomall.po.CouponPO;
import com.example.tomatomall.po.UserCouponPO;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.CouponDistributionLogRepository;
import com.example.tomatomall.repository.CouponRepository;
import com.example.tomatomall.repository.UserCouponRepository;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.CouponDistributionLogVO;
import com.example.tomatomall.vo.CouponDistributionRequestVO;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.UserCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponDistributionLogRepository couponDistributionLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public CouponVO createCoupon(CouponVO couponVO) {
        try {
            System.out.println("开始创建促销券: " + couponVO);
            
            CouponPO couponPO = new CouponPO();
            couponPO.setName(couponVO.getName());
            couponPO.setDescription(couponVO.getDescription());
            couponPO.setDiscountAmount(couponVO.getDiscountAmount());
            couponPO.setMinOrderAmount(couponVO.getMinOrderAmount());
            
            // 使用更健壮的日期解析
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String startDateStr = couponVO.getStartDate().trim();
                String endDateStr = couponVO.getEndDate().trim();
                
                System.out.println("解析开始日期: " + startDateStr);
                couponPO.setStartDate(LocalDateTime.parse(startDateStr, formatter));
                
                System.out.println("解析结束日期: " + endDateStr);
                couponPO.setEndDate(LocalDateTime.parse(endDateStr, formatter));
            } catch (Exception e) {
                System.err.println("日期解析失败: " + e.getMessage());
                throw new TomatoMallException(400, "日期格式不正确，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
            
            couponPO.setTotalQuantity(couponVO.getTotalQuantity());
            couponPO.setRemainingQuantity(couponVO.getTotalQuantity()); // 初始剩余数量等于总数量
            
            LocalDateTime now = LocalDateTime.now();
            couponPO.setCreatedAt(now);
            couponPO.setStatus(couponVO.getStatus() != null ? couponVO.getStatus() : 1); // 默认状态为有效
            
            System.out.println("准备保存促销券: " + couponPO);
            CouponPO savedCoupon = couponRepository.save(couponPO);
            System.out.println("促销券保存成功: " + savedCoupon);
            
            return CouponVO.fromPO(savedCoupon);
        } catch (TomatoMallException e) {
            System.err.println("创建促销券失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("创建促销券失败: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("根本原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new TomatoMallException(500, "创建促销券失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CouponVO updateCoupon(CouponVO couponVO) {
        try {
            System.out.println("开始更新促销券: " + couponVO);
            
            Optional<CouponPO> optionalCoupon = couponRepository.findById(couponVO.getId());
            if (!optionalCoupon.isPresent()) {
                throw new TomatoMallException(404, "促销券不存在");
            }
            
            CouponPO couponPO = optionalCoupon.get();
            couponPO.setName(couponVO.getName());
            couponPO.setDescription(couponVO.getDescription());
            couponPO.setDiscountAmount(couponVO.getDiscountAmount());
            couponPO.setMinOrderAmount(couponVO.getMinOrderAmount());
            
            // 使用更健壮的日期解析
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String startDateStr = couponVO.getStartDate().trim();
                String endDateStr = couponVO.getEndDate().trim();
                
                System.out.println("解析开始日期: " + startDateStr);
                couponPO.setStartDate(LocalDateTime.parse(startDateStr, formatter));
                
                System.out.println("解析结束日期: " + endDateStr);
                couponPO.setEndDate(LocalDateTime.parse(endDateStr, formatter));
            } catch (Exception e) {
                System.err.println("日期解析失败: " + e.getMessage());
                throw new TomatoMallException(400, "日期格式不正确，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
            
            // 如果总数量有变化，需要更新剩余数量
            if (!couponPO.getTotalQuantity().equals(couponVO.getTotalQuantity())) {
                int distributed = couponPO.getTotalQuantity() - couponPO.getRemainingQuantity();
                int newRemaining = couponVO.getTotalQuantity() - distributed;
                if (newRemaining < 0) {
                    newRemaining = 0;
                }
                couponPO.setTotalQuantity(couponVO.getTotalQuantity());
                couponPO.setRemainingQuantity(newRemaining);
            }
            
            couponPO.setStatus(couponVO.getStatus());
            
            System.out.println("准备保存更新后的促销券: " + couponPO);
            CouponPO updatedCoupon = couponRepository.save(couponPO);
            System.out.println("促销券更新成功: " + updatedCoupon);
            
            return CouponVO.fromPO(updatedCoupon);
        } catch (TomatoMallException e) {
            System.err.println("更新促销券失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("更新促销券失败: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("根本原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new TomatoMallException(500, "更新促销券失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteCoupon(Long couponId) {
        try {
            Optional<CouponPO> optionalCoupon = couponRepository.findById(couponId);
            if (!optionalCoupon.isPresent()) {
                throw new TomatoMallException(404, "促销券不存在");
            }
            
            // 检查是否有用户已领取该促销券
            long userCouponCount = userCouponRepository.countByCouponId(couponId);
            if (userCouponCount > 0) {
                // 如果已有用户领取，则将状态设置为无效，而不是物理删除
                CouponPO couponPO = optionalCoupon.get();
                couponPO.setStatus(0); // 设置为无效
                couponRepository.save(couponPO);
            } else {
                // 如果没有用户领取，则可以物理删除
                couponRepository.deleteById(couponId);
            }
        } catch (TomatoMallException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("删除促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "删除促销券失败: " + e.getMessage());
        }
    }

    @Override
    public CouponVO getCouponById(Long couponId) {
        try {
            Optional<CouponPO> optionalCoupon = couponRepository.findById(couponId);
            if (!optionalCoupon.isPresent()) {
                throw new TomatoMallException(404, "促销券不存在");
            }
            
            return CouponVO.fromPO(optionalCoupon.get());
        } catch (TomatoMallException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("获取促销券详情失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取促销券详情失败: " + e.getMessage());
        }
    }

    @Override
    public List<CouponVO> getAllCoupons() {
        try {
            List<CouponPO> couponPOs = couponRepository.findAll();
            return couponPOs.stream()
                    .map(CouponVO::fromPO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取所有促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取所有促销券失败: " + e.getMessage());
        }
    }

    @Override
    public List<CouponVO> getValidCoupons() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<CouponPO> validCoupons = couponRepository.findValidCoupons(now);
            return validCoupons.stream()
                    .map(CouponVO::fromPO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取有效促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取有效促销券失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<UserCouponVO> distributeCoupons(CouponDistributionRequestVO requestVO, Long adminId) {
        try {
            // 验证管理员身份
            Optional<UserPO> adminOpt = userRepository.findById(adminId);
            if (!adminOpt.isPresent()) {
                throw new TomatoMallException(404, "管理员不存在");
            }
            
            UserPO admin = adminOpt.get();
            if (!"admin".equals(admin.getRole())) {
                throw new TomatoMallException(403, "无权限执行此操作");
            }
            
            // 验证促销券
            Optional<CouponPO> couponOpt = couponRepository.findById(requestVO.getCouponId());
            if (!couponOpt.isPresent()) {
                throw new TomatoMallException(404, "促销券不存在");
            }
            
            CouponPO coupon = couponOpt.get();
            if (coupon.getStatus() != 1) {
                throw new TomatoMallException(400, "促销券已失效");
            }
            
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(coupon.getEndDate())) {
                throw new TomatoMallException(400, "促销券已过期");
            }
            
            if (coupon.getRemainingQuantity() <= 0) {
                throw new TomatoMallException(400, "促销券库存不足，发放失败");
            }
            
            List<Long> userIds;
            // 如果指定了用户ID列表，则发放给这些用户
            if (requestVO.getUserIds() != null && !requestVO.getUserIds().isEmpty()) {
                userIds = requestVO.getUserIds();
            } else {
                // 否则根据条件筛选用户
                // 这里可以根据实际需求实现更复杂的用户筛选逻辑
                // 简单起见，这里获取所有普通用户
                List<UserPO> users = userRepository.findAll().stream()
                        .filter(user -> "user".equals(user.getRole()))
                        .collect(Collectors.toList());
                userIds = users.stream().map(UserPO::getId).collect(Collectors.toList());
            }
            
            // 检查库存是否足够
            if (coupon.getRemainingQuantity() < userIds.size()) {
                throw new TomatoMallException(400, "促销券库存不足，发放失败");
            }
            
            // 发放促销券
            List<UserCouponPO> userCoupons = new ArrayList<>();
            int successCount = 0;
            
            for (Long userId : userIds) {
                // 检查用户是否存在
                Optional<UserPO> userOpt = userRepository.findById(userId);
                if (!userOpt.isPresent()) {
                    continue;
                }
                
                // 创建用户促销券关联
                UserCouponPO userCoupon = new UserCouponPO();
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(coupon.getId());
                userCoupon.setIsUsed(0); // 未使用
                userCoupon.setCreatedAt(now);
                
                userCoupons.add(userCoupon);
                successCount++;
            }
            
            if (successCount == 0) {
                throw new TomatoMallException(400, "没有符合条件的用户可以发放促销券");
            }
            
            // 批量保存用户促销券关联
            List<UserCouponPO> savedUserCoupons = userCouponRepository.saveAll(userCoupons);
            
            // 更新促销券剩余数量
            coupon.setRemainingQuantity(coupon.getRemainingQuantity() - successCount);
            couponRepository.save(coupon);
            
            // 记录发放日志
            CouponDistributionLogPO log = new CouponDistributionLogPO();
            log.setAdminId(adminId);
            log.setCouponId(coupon.getId());
            log.setDistributionTime(now);
            log.setDistributionCount(successCount);
            log.setDistributionCondition(requestVO.getDistributionCondition());
            log.setRemark(requestVO.getRemark());
            couponDistributionLogRepository.save(log);
            
            System.out.println("成功发放促销券给 " + successCount + " 个用户");
            
            // 转换为VO返回
            return savedUserCoupons.stream()
                    .map(UserCouponVO::fromPO)
                    .collect(Collectors.toList());
        } catch (TomatoMallException e) {
            System.err.println("发放促销券失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("发放促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "发放促销券失败: " + e.getMessage());
        }
    }

    @Override
    public List<UserCouponVO> getUserCoupons(Long userId) {
        try {
            List<UserCouponPO> userCoupons = userCouponRepository.findByUserId(userId);
            return userCoupons.stream()
                    .map(UserCouponVO::fromPO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取用户促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取用户促销券失败: " + e.getMessage());
        }
    }

    @Override
    public List<UserCouponVO> getUserUnusedCoupons(Long userId) {
        try {
            List<UserCouponPO> userCoupons = userCouponRepository.findByUserIdAndIsUsed(userId, 0);
            return userCoupons.stream()
                    .map(UserCouponVO::fromPO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取用户未使用促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取用户未使用促销券失败: " + e.getMessage());
        }
    }

    @Override
    public List<CouponDistributionLogVO> getCouponDistributionLogs(Long couponId) {
        try {
            List<CouponDistributionLogPO> logs = couponDistributionLogRepository.findByCouponId(couponId);
            return logs.stream()
                    .map(CouponDistributionLogVO::fromPO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取促销券发放记录失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取促销券发放记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<CouponDistributionLogVO> getAdminCouponDistributionLogs(Long adminId) {
        try {
            List<CouponDistributionLogPO> logs = couponDistributionLogRepository.findByAdminId(adminId);
            return logs.stream()
                    .map(CouponDistributionLogVO::fromPO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取管理员促销券发放记录失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取管理员促销券发放记录失败: " + e.getMessage());
        }
    }

    @Override
    public UserCouponVO getUserCouponById(Long userCouponId) {
        try {
            Optional<UserCouponPO> optionalUserCoupon = userCouponRepository.findById(userCouponId);
            if (!optionalUserCoupon.isPresent()) {
                throw new TomatoMallException(404, "用户促销券不存在");
            }
            
            return UserCouponVO.fromPO(optionalUserCoupon.get());
        } catch (TomatoMallException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("获取用户促销券详情失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "获取用户促销券详情失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public UserCouponVO useCoupon(Long userCouponId, Long userId, Long orderId, BigDecimal orderAmount) {
        try {
            // 查询用户促销券
            Optional<UserCouponPO> optionalUserCoupon = userCouponRepository.findById(userCouponId);
            if (!optionalUserCoupon.isPresent()) {
                throw new TomatoMallException(404, "促销券不存在");
            }
            
            UserCouponPO userCoupon = optionalUserCoupon.get();
            
            // 验证用户身份
            if (!userCoupon.getUserId().equals(userId)) {
                throw new TomatoMallException(403, "无权使用此促销券");
            }
            
            // 检查促销券是否已使用
            if (userCoupon.getIsUsed() == 1) {
                throw new TomatoMallException(400, "促销券已使用");
            }
            
            // 获取促销券详情
            Optional<CouponPO> optionalCoupon = couponRepository.findById(userCoupon.getCouponId());
            if (!optionalCoupon.isPresent()) {
                throw new TomatoMallException(404, "促销券信息不存在");
            }
            
            CouponPO coupon = optionalCoupon.get();
            
            // 检查促销券是否有效
            LocalDateTime now = LocalDateTime.now();
            if (coupon.getStatus() != 1) {
                throw new TomatoMallException(400, "促销券已失效");
            }
            
            if (now.isBefore(coupon.getStartDate())) {
                throw new TomatoMallException(400, "促销券尚未生效");
            }
            
            if (now.isAfter(coupon.getEndDate())) {
                throw new TomatoMallException(400, "促销券已过期");
            }
            
            // 检查订单金额是否满足最低要求
            if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new TomatoMallException(400, "订单金额不满足促销券使用条件，最低需要" + coupon.getMinOrderAmount() + "元");
            }
            
            // 更新促销券状态为已使用
            userCoupon.setIsUsed(1);
            userCoupon.setUsedTime(now);
            userCoupon.setOrderId(orderId.intValue());
            
            UserCouponPO updatedUserCoupon = userCouponRepository.save(userCoupon);
            
            return UserCouponVO.fromPO(updatedUserCoupon);
        } catch (TomatoMallException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("使用促销券失败: " + e.getMessage());
            e.printStackTrace();
            throw new TomatoMallException(500, "使用促销券失败: " + e.getMessage());
        }
    }
} 