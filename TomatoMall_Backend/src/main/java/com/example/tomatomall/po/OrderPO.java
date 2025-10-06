package com.example.tomatomall.po;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "create_time", insertable = false, updatable = false)
    private Timestamp createTime;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_zipcode", length = 20)
    private String receiverZipcode;

    @Column(name = "receiver_address", length = 255)
    private String receiverAddress;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItemPO> orderItems = new ArrayList<>();

    @Column(name = "alipay_trade_no", length = 64)
    private String alipayTradeNo;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "expire_time")
    private Timestamp expireTime;

    // Add helper method to add order items
    public void addOrderItem(OrderItemPO item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}
