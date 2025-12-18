package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.CartItemPO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.repository.CartItemRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CartServiceImpl cartService;

    private ProductPO testProduct;
    private StockpilePO testStockpile;
    private CartItemPO testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = new ProductPO();
        testProduct.setId(1L);
        testProduct.setTitle("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");
        testProduct.setCover("test-cover.jpg");
        testProduct.setDetail("Test Detail");

        testStockpile = new StockpilePO();
        testStockpile.setId(1L);
        testStockpile.setAmount(100);
        testStockpile.setFrozen(0);
        testStockpile.setProduct(testProduct);
        testProduct.setStockpile(testStockpile);

        testCartItem = new CartItemPO();
        testCartItem.setCartItemId(1L);
        testCartItem.setUserId(1L);
        testCartItem.setProductId(1L);
        testCartItem.setQuantity(2);
    }

    @Test
    void testAddToCart_NewItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItemPO.class))).thenAnswer(invocation -> {
            CartItemPO item = invocation.getArgument(0);
            item.setCartItemId(1L);
            return item;
        });

        CartItemVO result = cartService.addToCart(1L, 1L, 3);

        assertNotNull(result);
        assertEquals("Test Product", result.getTitle());
        verify(cartItemRepository, times(1)).save(any(CartItemPO.class));
    }

    @Test
    void testAddToCart_ExistingItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItemPO.class))).thenReturn(testCartItem);

        CartItemVO result = cartService.addToCart(1L, 1L, 3);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItemPO.class));
    }

    @Test
    void testAddToCart_ProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.addToCart(1L, 999L, 3);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testAddToCart_InsufficientStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.addToCart(1L, 1L, 200);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAddToCart_ExceedsStockWhenAdding() {
        testCartItem.setQuantity(95);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.addToCart(1L, 1L, 10);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testRemoveFromCart_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(any(CartItemPO.class));

        String result = cartService.removeFromCart(1L, 1L);

        assertEquals("删除成功", result);
        verify(cartItemRepository, times(1)).delete(testCartItem);
    }

    @Test
    void testRemoveFromCart_NotFound() {
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.removeFromCart(1L, 999L);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testRemoveFromCart_UnauthorizedUser() {
        testCartItem.setUserId(2L);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.removeFromCart(1L, 1L);
        });
        assertEquals(403, exception.getCode());
    }

    @Test
    void testUpdateCartItemQuantity_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItemPO.class))).thenReturn(testCartItem);

        String result = cartService.updateCartItemQuantity(1L, 1L, 5);

        assertEquals("修改数量成功", result);
        verify(cartItemRepository, times(1)).save(any(CartItemPO.class));
    }

    @Test
    void testUpdateCartItemQuantity_InvalidQuantity() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, 0);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUpdateCartItemQuantity_NegativeQuantity() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, -5);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUpdateCartItemQuantity_CartItemNotFound() {
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.updateCartItemQuantity(1L, 999L, 5);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUpdateCartItemQuantity_UnauthorizedUser() {
        testCartItem.setUserId(2L);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, 5);
        });
        assertEquals(403, exception.getCode());
    }

    @Test
    void testUpdateCartItemQuantity_InsufficientStock() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.updateCartItemQuantity(1L, 1L, 200);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testGetCartItems_Success() {
        CartItemPO item1 = new CartItemPO();
        item1.setCartItemId(1L);
        item1.setUserId(1L);
        item1.setProductId(1L);
        item1.setQuantity(2);

        CartItemPO item2 = new CartItemPO();
        item2.setCartItemId(2L);
        item2.setUserId(1L);
        item2.setProductId(2L);
        item2.setQuantity(1);

        ProductPO product2 = new ProductPO();
        product2.setId(2L);
        product2.setTitle("Product 2");
        product2.setPrice(new BigDecimal("49.99"));
        product2.setDescription("Description 2");

        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(item1, item2));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

        CartVO result = cartService.getCartItems(1L);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getItems().size());
        assertTrue(result.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetCartItems_EmptyCart() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        CartVO result = cartService.getCartItems(1L);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getItems().size());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }

    @Test
    void testCheckout_Success() {
        when(cartItemRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(OrderPO.class))).thenAnswer(invocation -> {
            OrderPO order = invocation.getArgument(0);
            order.setOrderId(1L);
            return order;
        });
        doNothing().when(cartItemRepository).deleteAll(anyList());

        OrderVO result = cartService.checkout(
                1L,
                Arrays.asList("1"),
                "John Doe",
                "13800138000",
                "100000",
                "Test Address",
                "Alipay",
                null
        );

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        verify(orderRepository, times(1)).save(any(OrderPO.class));
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void testCheckout_EmptyCart() {
        when(cartItemRepository.findAllById(anyList())).thenReturn(Arrays.asList());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.checkout(
                    1L,
                    Arrays.asList("1"),
                    "John Doe",
                    "13800138000",
                    "100000",
                    "Test Address",
                    "Alipay",
                    null
            );
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testCheckout_InsufficientStock() {
        testStockpile.setAmount(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.checkout(
                    1L,
                    Arrays.asList("1"),
                    "John Doe",
                    "13800138000",
                    "100000",
                    "Test Address",
                    "Alipay",
                    null
            );
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testCheckout_ProductNotFound() {
        when(cartItemRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            cartService.checkout(
                    1L,
                    Arrays.asList("1"),
                    "John Doe",
                    "13800138000",
                    "100000",
                    "Test Address",
                    "Alipay",
                    null
            );
        });
        assertEquals(404, exception.getCode());
    }
}
