package com.example.tomatomall.vo;

import com.example.tomatomall.po.BannerPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerVO {
    private Long id;
    private String title;
    private String description;
    private String link;
    private Integer displayOrder;
    private List<ProductVO> books; // 存储书籍对象列表

    public static BannerVO fromPO(BannerPO bannerPO) {
        BannerVO vo = new BannerVO();
        vo.setId(bannerPO.getId());
        vo.setTitle(bannerPO.getTitle());
        vo.setDescription(bannerPO.getDescription());
        vo.setLink(bannerPO.getLink());
        vo.setDisplayOrder(bannerPO.getDisplayOrder());
        return vo;
    }
} 