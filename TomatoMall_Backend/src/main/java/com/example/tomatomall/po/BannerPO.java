package com.example.tomatomall.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "banner")
public class BannerPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String link;

    @Column(name = "display_order")
    private Integer displayOrder; // 显示顺序

    @Column(name = "product_ids", length = 1000)
    private String productIds; // 存储书籍ID，格式为逗号分隔的ID列表，如"1,2,3,4"
} 