package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.CartItemPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.repository.CartItemRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public CartItemVO addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在
        ProductPO product = productRepository.findById(productId)
                .orElseThrow(() -> new TomatoMallException(404, "商品不存在"));

        // 检查库存是否足够
        StockpilePO stockpile = product.getStockpile();
        if (stockpile == null || stockpile.getAmount() < quantity) {
            throw new TomatoMallException(400, "商品库存不足");
        }

        // 查询该用户的购物车中是否已有该商品
        Optional<CartItemPO> existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        CartItemPO cartItem;
        if (existingCartItem.isPresent()) {
            // 如果已有该商品，则增加数量
            cartItem = existingCartItem.get();
            // 检查增加后的数量是否超过库存
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > stockpile.getAmount()) {
                throw new TomatoMallException(400, "商品库存不足");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            // 如果没有该商品，则新增一条记录
            cartItem = new CartItemPO();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
        }

        // 保存购物车记录
        cartItem = cartItemRepository.save(cartItem);

        // 转换为VO返回
        return convertToCartItemVO(cartItem, product);
    }

    @Override
    @Transactional
    public String removeFromCart(Long userId, Long cartItemId) {
        // 查询该购物车项是否存在
        CartItemPO cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new TomatoMallException(400, "购物车商品不存在"));

        // 验证该购物车项是否属于当前用户
        if (!cartItem.getUserId().equals(userId)) {
            throw new TomatoMallException(403, "无权操作此购物车商品");
        }

        // 删除该购物车项
        cartItemRepository.delete(cartItem);
        return "删除成功";
    }

    @Override
    @Transactional
    public String updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        // 检查数量是否合法
        if (quantity <= 0) {
            throw new TomatoMallException(400, "商品数量必须大于0");
        }

        // 查询该购物车项是否存在
        CartItemPO cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new TomatoMallException(400, "购物车商品不存在"));

        // 验证该购物车项是否属于当前用户
        if (!cartItem.getUserId().equals(userId)) {
            throw new TomatoMallException(403, "无权操作此购物车商品");
        }

        // 检查库存是否足够
        ProductPO product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new TomatoMallException(404, "商品不存在"));
        StockpilePO stockpile = product.getStockpile();
        if (stockpile == null || stockpile.getAmount() < quantity) {
            throw new TomatoMallException(400, "商品库存不足");
        }

        // 更新数量
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        return "修改数量成功";
    }

    @Override
    public CartVO getCartItems(Long userId) {
        // 查询该用户的所有购物车项
        List<CartItemPO> cartItems = cartItemRepository.findByUserId(userId);
        
        List<CartItemVO> cartItemVOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 转换为VO列表并计算总金额
        for (CartItemPO cartItem : cartItems) {
            ProductPO product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new TomatoMallException(404, "商品不存在"));
            
            CartItemVO cartItemVO = convertToCartItemVO(cartItem, product);
            cartItemVOs.add(cartItemVO);
            
            // 计算该商品总价并累加到购物车总金额
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        
        // 构建购物车VO
        CartVO cartVO = new CartVO();
        cartVO.setItems(cartItemVOs);
        cartVO.setTotal(cartItemVOs.size());
        cartVO.setTotalAmount(totalAmount);
        
        return cartVO;
    }
    
    private CartItemVO convertToCartItemVO(CartItemPO cartItem, ProductPO product) {
        CartItemVO cartItemVO = new CartItemVO();
        cartItemVO.setCartItemId(cartItem.getCartItemId());
        cartItemVO.setProductId(product.getId());
        cartItemVO.setTitle(product.getTitle());
        cartItemVO.setPrice(product.getPrice());
        cartItemVO.setDescription(product.getDescription());
        cartItemVO.setCover(product.getCover());
        cartItemVO.setDetail(product.getDetail());
        cartItemVO.setQuantity(cartItem.getQuantity());
        return cartItemVO;
    }
} 