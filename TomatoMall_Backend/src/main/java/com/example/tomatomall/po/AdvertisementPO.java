package com.example.tomatomall.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "advertisements")
public class AdvertisementPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "广告标题不能为空")
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotBlank(message = "广告内容不能为空")
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @NotBlank(message = "广告图片URL不能为空")
    @Column(name = "image_url", nullable = false, length = 500)
    private String imgUrl;

    @NotNull(message = "所属商品ID不能为空")
    @Column(name = "product_id", nullable = false)
    private Integer productId;
} 