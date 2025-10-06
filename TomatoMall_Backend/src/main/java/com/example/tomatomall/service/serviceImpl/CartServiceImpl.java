package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.CartItemPO;
import com.example.tomatomall.po.OrderItemPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.repository.CartItemRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderItemVO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

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

    @Override
    @Transactional
    public OrderVO checkout(Long userId, List<String> cartItemIds, String receiverName,
            String receiverPhone, String receiverZipcode, String receiverAddress, String paymentMethod) {
        // 1. 查询购物车商品
        List<Long> cartItemIdList = cartItemIds.stream().map(Long::parseLong).collect(Collectors.toList());
        List<CartItemPO> cartItems = cartItemRepository.findAllById(cartItemIdList);
        if (cartItems.isEmpty()) {
            throw new TomatoMallException(400, "购物车为空");
        }
        // 2. 校验库存并计算总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemPO item : cartItems) {
            ProductPO product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new TomatoMallException(404, "商品不存在: " + item.getProductId()));
            StockpilePO stockpile = product.getStockpile();
            if (stockpile == null || stockpile.getAmount() < item.getQuantity()) {
                throw new TomatoMallException(400, "库存不足: " + product.getTitle());
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 3. 扣减库存
        for (CartItemPO item : cartItems) {
            ProductPO product = productRepository.findById(item.getProductId()).get();
            StockpilePO stockpile = product.getStockpile();
            stockpile.setAmount(stockpile.getAmount() - item.getQuantity());
            // 保存库存
            productRepository.save(product);
        }
        // 4. 创建订单
        OrderPO order = new OrderPO();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING");
        order.setCreateTime(new Timestamp(System.currentTimeMillis()));
        // 设置订单15分钟后超时
        order.setExpireTime(new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000));
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverZipcode(receiverZipcode);
        order.setReceiverAddress(receiverAddress);
        // 添加订单项
        List<OrderItemVO> orderItemVOs = new ArrayList<>();
        for (CartItemPO cartItem : cartItems) {
            ProductPO product = productRepository.findById(cartItem.getProductId()).get();

            // 创建订单项
            OrderItemPO orderItem = new OrderItemPO();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setProductTitle(product.getTitle());
            orderItem.setProductCover(product.getCover());
            order.addOrderItem(orderItem);

            // 创建VO
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, itemVO);
            itemVO.setSubtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItemVOs.add(itemVO);
        }

        try {
            order = orderRepository.save(order);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细异常
            throw new RuntimeException("保存订单失败: " + e.getMessage(), e);
        }
        // 5. 删除已结算购物车项
        cartItemRepository.deleteAll(cartItems);
        // 6. 返回OrderVO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderItems(orderItemVOs);
        return orderVO;
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