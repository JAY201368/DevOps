package com.example.tomatomall.controller;

import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import com.example.tomatomall.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvertisementController.class);

    private final AdvertisementService advertisementService;

    @Autowired
    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping
    public ResultVO<List<AdvertisementVO>> getAllAdvertisements() {
        logger.info("获取所有广告信息");
        List<AdvertisementVO> advertisements = advertisementService.getAllAdvertisements();
        return ResultVO.buildSuccess(advertisements);
    }

    @PostMapping
    public ResultVO<AdvertisementVO> createAdvertisement(@RequestBody AdvertisementVO advertisementVO) {
        logger.info("创建广告, 广告信息: {}", advertisementVO);
        AdvertisementVO created = advertisementService.createAdvertisement(advertisementVO);
        if (created == null) {
            logger.error("创建广告失败: 商品不存在, productId: {}", advertisementVO.getProductId());
            return ResultVO.buildFailure("商品不存在", "400");
        }
        logger.info("创建广告成功: {}", created);
        return ResultVO.buildSuccess(created);
    }

    @PutMapping
    public ResultVO<String> updateAdvertisement(@RequestBody AdvertisementVO advertisementVO) {
        logger.info("更新广告, 广告信息: {}", advertisementVO);
        String result = advertisementService.updateAdvertisement(advertisementVO);
        if (result == null) {
            logger.error("更新广告失败: 商品不存在, productId: {}", advertisementVO.getProductId());
            return ResultVO.buildFailure("商品不存在", "400");
        }
        logger.info("更新广告成功");
        return ResultVO.buildSuccess(result);
    }

    @DeleteMapping("/{id}")
    public ResultVO<String> deleteAdvertisement(@PathVariable String id) {
        logger.info("删除广告, 广告ID: {}", id);
        try {
            String result = advertisementService.deleteAdvertisement(Integer.valueOf(id));
            if (result == null) {
                logger.error("删除广告失败: 广告不存在, 广告ID: {}", id);
                return ResultVO.buildFailure("广告不存在", "400");
            }
            logger.info("删除广告成功");
            return ResultVO.buildSuccess(result);
        } catch (NumberFormatException e) {
            logger.error("删除广告失败: ID格式错误: {}", id, e);
            return ResultVO.buildFailure("广告ID格式错误", "400");
        } catch (Exception e) {
            logger.error("删除广告时发生未知错误: {}", id, e);
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
} 