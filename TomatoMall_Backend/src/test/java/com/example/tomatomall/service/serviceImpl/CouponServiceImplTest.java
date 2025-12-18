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
import com.example.tomatomall.vo.CouponDistributionRequestVO;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.UserCouponVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponDistributionLogRepository couponDistributionLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private CouponPO testCoupon;
    private CouponVO testCouponVO;
    private UserPO testUser;
    private UserPO adminUser;

    @BeforeEach
    void setUp() {
        testCoupon = new CouponPO();
        testCoupon.setId(1L);
        testCoupon.setName("Test Coupon");
        testCoupon.setDescription("Test Description");
        testCoupon.setDiscountAmount(new BigDecimal("10.00"));
        testCoupon.setMinOrderAmount(new BigDecimal("100.00"));
        testCoupon.setStartDate(LocalDateTime.now().minusDays(1));
        testCoupon.setEndDate(LocalDateTime.now().plusDays(30));
        testCoupon.setTotalQuantity(100);
        testCoupon.setRemainingQuantity(100);
        testCoupon.setStatus(1);
        testCoupon.setCreatedAt(LocalDateTime.now());

        testCouponVO = new CouponVO();
        testCouponVO.setName("Test Coupon");
        testCouponVO.setDescription("Test Description");
        testCouponVO.setDiscountAmount(new BigDecimal("10.00"));
        testCouponVO.setMinOrderAmount(new BigDecimal("100.00"));
        testCouponVO.setStartDate("2024-01-01 00:00:00");
        testCouponVO.setEndDate("2024-12-31 23:59:59");
        testCouponVO.setTotalQuantity(100);
        testCouponVO.setStatus(1);

        testUser = new UserPO();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("user");

        adminUser = new UserPO();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole("admin");
    }

    @Test
    void testCreateCoupon_Success() {
        when(couponRepository.save(any(CouponPO.class))).thenAnswer(invocation -> {
            CouponPO coupon = invocation.getArgument(0);
            coupon.setId(1L);
            return coupon;
        });

        CouponVO result = couponService.createCoupon(testCouponVO);

        assertNotNull(result);
        assertEquals("Test Coupon", result.getName());
        verify(couponRepository, times(1)).save(any(CouponPO.class));
    }

    @Test
    void testCreateCoupon_InvalidDateFormat() {
        testCouponVO.setStartDate("invalid-date");

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.createCoupon(testCouponVO);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUpdateCoupon_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(CouponPO.class))).thenReturn(testCoupon);

        testCouponVO.setId(1L);
        testCouponVO.setName("Updated Coupon");

        CouponVO result = couponService.updateCoupon(testCouponVO);

        assertNotNull(result);
        verify(couponRepository, times(1)).save(any(CouponPO.class));
    }

    @Test
    void testUpdateCoupon_NotFound() {
        testCouponVO.setId(999L);
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.updateCoupon(testCouponVO);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testUpdateCoupon_UpdateQuantity() {
        testCoupon.setRemainingQuantity(90);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(CouponPO.class))).thenReturn(testCoupon);

        testCouponVO.setId(1L);
        testCouponVO.setTotalQuantity(150);

        CouponVO result = couponService.updateCoupon(testCouponVO);

        assertNotNull(result);
        verify(couponRepository, times(1)).save(any(CouponPO.class));
    }

    @Test
    void testDeleteCoupon_NoUserCoupons() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(userCouponRepository.countByCouponId(1L)).thenReturn(0L);
        doNothing().when(couponRepository).deleteById(1L);

        couponService.deleteCoupon(1L);

        verify(couponRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCoupon_WithUserCoupons() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(userCouponRepository.countByCouponId(1L)).thenReturn(5L);
        when(couponRepository.save(any(CouponPO.class))).thenReturn(testCoupon);

        couponService.deleteCoupon(1L);

        verify(couponRepository, times(0)).deleteById(1L);
        verify(couponRepository, times(1)).save(any(CouponPO.class));
    }

    @Test
    void testDeleteCoupon_NotFound() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.deleteCoupon(999L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testGetCouponById_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        CouponVO result = couponService.getCouponById(1L);

        assertNotNull(result);
        assertEquals("Test Coupon", result.getName());
    }

    @Test
    void testGetCouponById_NotFound() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.getCouponById(999L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testGetAllCoupons() {
        when(couponRepository.findAll()).thenReturn(Arrays.asList(testCoupon));

        var result = couponService.getAllCoupons();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(couponRepository, times(1)).findAll();
    }

    @Test
    void testGetValidCoupons() {
        when(couponRepository.findValidCoupons(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testCoupon));

        var result = couponService.getValidCoupons();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDistributeCoupons_Success() {
        CouponDistributionRequestVO requestVO = new CouponDistributionRequestVO();
        requestVO.setCouponId(1L);
        requestVO.setUserIds(Arrays.asList(1L));
        requestVO.setDistributionCondition("Test distribution");
        requestVO.setRemark("Test remark");

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userCouponRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(couponRepository.save(any(CouponPO.class))).thenReturn(testCoupon);
        when(couponDistributionLogRepository.save(any(CouponDistributionLogPO.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = couponService.distributeCoupons(requestVO, 2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userCouponRepository, times(1)).saveAll(anyList());
        verify(couponRepository, times(1)).save(any(CouponPO.class));
    }

    @Test
    void testDistributeCoupons_NotAdmin() {
        CouponDistributionRequestVO requestVO = new CouponDistributionRequestVO();
        requestVO.setCouponId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.distributeCoupons(requestVO, 1L);
        });
        assertEquals(403, exception.getCode());
    }

    @Test
    void testDistributeCoupons_CouponNotFound() {
        CouponDistributionRequestVO requestVO = new CouponDistributionRequestVO();
        requestVO.setCouponId(999L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.distributeCoupons(requestVO, 2L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testDistributeCoupons_CouponExpired() {
        CouponDistributionRequestVO requestVO = new CouponDistributionRequestVO();
        requestVO.setCouponId(1L);

        testCoupon.setEndDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.distributeCoupons(requestVO, 2L);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testDistributeCoupons_InsufficientStock() {
        CouponDistributionRequestVO requestVO = new CouponDistributionRequestVO();
        requestVO.setCouponId(1L);
        requestVO.setUserIds(Arrays.asList(1L, 2L, 3L));

        testCoupon.setRemainingQuantity(1);
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.distributeCoupons(requestVO, 2L);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testGetUserCoupons() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setCoupon(testCoupon);
        userCoupon.setIsUsed(0);
        userCoupon.setCreatedAt(LocalDateTime.now());

        when(userCouponRepository.findByUserId(1L)).thenReturn(Arrays.asList(userCoupon));

        var result = couponService.getUserCoupons(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUserUnusedCoupons() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setCoupon(testCoupon);
        userCoupon.setIsUsed(0);
        userCoupon.setCreatedAt(LocalDateTime.now());

        when(userCouponRepository.findByUserIdAndIsUsed(1L, 0)).thenReturn(Arrays.asList(userCoupon));

        var result = couponService.getUserUnusedCoupons(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUserCouponById_Success() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setCoupon(testCoupon);
        userCoupon.setCreatedAt(LocalDateTime.now());

        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));

        UserCouponVO result = couponService.getUserCouponById(1L);

        assertNotNull(result);
    }

    @Test
    void testGetUserCouponById_NotFound() {
        when(userCouponRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.getUserCouponById(999L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testUseCoupon_Success() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setCoupon(testCoupon);
        userCoupon.setIsUsed(0);
        userCoupon.setCreatedAt(LocalDateTime.now());

        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(userCouponRepository.save(any(UserCouponPO.class))).thenAnswer(invocation -> {
            UserCouponPO saved = invocation.getArgument(0);
            saved.setCoupon(testCoupon);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        UserCouponVO result = couponService.useCoupon(1L, 1L, 100L, new BigDecimal("150.00"));

        assertNotNull(result);
        verify(userCouponRepository, times(1)).save(any(UserCouponPO.class));
    }

    @Test
    void testUseCoupon_NotFound() {
        when(userCouponRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.useCoupon(999L, 1L, 100L, new BigDecimal("150.00"));
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testUseCoupon_UnauthorizedUser() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(2L);
        userCoupon.setCouponId(1L);

        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.useCoupon(1L, 1L, 100L, new BigDecimal("150.00"));
        });
        assertEquals(403, exception.getCode());
    }

    @Test
    void testUseCoupon_AlreadyUsed() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setIsUsed(1);

        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.useCoupon(1L, 1L, 100L, new BigDecimal("150.00"));
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUseCoupon_OrderAmountTooLow() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setIsUsed(0);

        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.useCoupon(1L, 1L, 100L, new BigDecimal("50.00"));
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUseCoupon_CouponExpired() {
        UserCouponPO userCoupon = new UserCouponPO();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setIsUsed(0);

        testCoupon.setEndDate(LocalDateTime.now().minusDays(1));
        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            couponService.useCoupon(1L, 1L, 100L, new BigDecimal("150.00"));
        });
        assertEquals(400, exception.getCode());
    }
}
