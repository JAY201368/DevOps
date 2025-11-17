package com.example.tomatomall.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementVO {
    private String id;
    private String title;
    private String content;
    private String imgUrl;
    private String productId;
} 