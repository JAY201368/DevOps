package com.example.tomatomall.controller;

import com.example.tomatomall.po.BannerPO;
import com.example.tomatomall.service.BannerService;
import com.example.tomatomall.vo.BannerVO;
import com.example.tomatomall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取所有轮播图
     */
    @GetMapping
    public ResultVO<List<BannerVO>> getAllBanners() {
        try {
            List<BannerVO> banners = bannerService.getAllBanners();
            return ResultVO.buildSuccess(banners);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("获取轮播图失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 获取指定ID的轮播图
     */
    @GetMapping("/{id}")
    public ResultVO<BannerVO> getBannerById(@PathVariable Long id) {
        try {
            BannerVO banner = bannerService.getBannerById(id);
            if (banner == null) {
                return ResultVO.buildFailure("轮播图不存在", "404");
            }
            return ResultVO.buildSuccess(banner);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("获取轮播图失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 创建轮播图（仅管理员）
     */
    @PostMapping
    public ResultVO<BannerVO> createBanner(@RequestBody BannerPO bannerPO) {
        try {
            BannerVO banner = bannerService.createBanner(bannerPO);
            return ResultVO.buildSuccess(banner);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("创建轮播图失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 更新轮播图（仅管理员）
     */
    @PutMapping("/{id}")
    public ResultVO<BannerVO> updateBanner(@PathVariable Long id, @RequestBody BannerPO bannerPO) {
        try {
            BannerVO banner = bannerService.updateBanner(id, bannerPO);
            if (banner == null) {
                return ResultVO.buildFailure("轮播图不存在", "404");
            }
            return ResultVO.buildSuccess(banner);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("更新轮播图失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 删除轮播图（仅管理员）
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteBanner(@PathVariable Long id) {
        try {
            bannerService.deleteBanner(id);
            return ResultVO.buildSuccess(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("删除轮播图失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 更新轮播图的书籍（仅管理员）
     */
    @PutMapping("/{id}/books")
    public ResultVO<BannerVO> updateBannerBooks(@PathVariable Long id, @RequestBody Map<String, List<Long>> payload) {
        try {
            List<Long> productIds = payload.get("productIds");
            if (productIds == null) {
                return ResultVO.buildFailure("请提供书籍ID列表", "400");
            }
            
            BannerVO banner = bannerService.updateBannerBooks(id, productIds);
            if (banner == null) {
                return ResultVO.buildFailure("轮播图不存在", "404");
            }
            return ResultVO.buildSuccess(banner);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("更新轮播图书籍失败: " + e.getMessage(), "500");
        }
    }
}