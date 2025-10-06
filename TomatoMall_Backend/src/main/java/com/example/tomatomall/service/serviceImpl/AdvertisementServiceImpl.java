package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.AdvertisementPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.repository.AdvertisementRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    private static final Logger logger = LoggerFactory.getLogger(AdvertisementServiceImpl.class);
    
    private final AdvertisementRepository advertisementRepository;
    private final ProductRepository productRepository;

    @Autowired
    public AdvertisementServiceImpl(AdvertisementRepository advertisementRepository, ProductRepository productRepository) {
        this.advertisementRepository = advertisementRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<AdvertisementVO> getAllAdvertisements() {
        List<AdvertisementPO> advertisementPOS = advertisementRepository.findAll();
        return advertisementPOS.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public AdvertisementVO createAdvertisement(AdvertisementVO advertisementVO) {
        try {
            // 检查商品是否存在
            Long productId = Long.valueOf(advertisementVO.getProductId());
            Optional<ProductPO> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                logger.warn("创建广告失败: 商品不存在, productId: {}", productId);
                return null; // 商品不存在，返回null
            }

            AdvertisementPO advertisementPO = AdvertisementPO.builder()
                    .title(advertisementVO.getTitle())
                    .content(advertisementVO.getContent())
                    .imgUrl(advertisementVO.getImgUrl())
                    .productId(productId.intValue())
                    .build();

            AdvertisementPO savedPO = advertisementRepository.save(advertisementPO);
            logger.info("创建广告成功: {}", savedPO);
            return convertToVO(savedPO);
        } catch (NumberFormatException e) {
            logger.error("创建广告失败: 商品ID格式错误: {}", advertisementVO.getProductId(), e);
            return null;
        } catch (Exception e) {
            logger.error("创建广告时发生未知错误", e);
            return null;
        }
    }

    @Override
    public String updateAdvertisement(AdvertisementVO advertisementVO) {
        try {
            // 检查广告是否存在
            Integer adId = Integer.valueOf(advertisementVO.getId());
            Optional<AdvertisementPO> advertisementOpt = advertisementRepository.findById(adId);
            if (!advertisementOpt.isPresent()) {
                logger.warn("更新广告失败: 广告不存在, adId: {}", adId);
                return null;
            }

            // 检查商品是否存在
            Long productId = Long.valueOf(advertisementVO.getProductId());
            Optional<ProductPO> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                logger.warn("更新广告失败: 商品不存在, productId: {}", productId);
                return null; // 商品不存在，返回null
            }

            AdvertisementPO advertisementPO = advertisementOpt.get();
            if (advertisementVO.getTitle() != null) {
                advertisementPO.setTitle(advertisementVO.getTitle());
            }
            if (advertisementVO.getContent() != null) {
                advertisementPO.setContent(advertisementVO.getContent());
            }
            if (advertisementVO.getImgUrl() != null) {
                advertisementPO.setImgUrl(advertisementVO.getImgUrl());
            }
            advertisementPO.setProductId(productId.intValue());

            advertisementRepository.save(advertisementPO);
            logger.info("更新广告成功: {}", advertisementPO);
            return "更新成功";
        } catch (NumberFormatException e) {
            logger.error("更新广告失败: ID格式错误: adId: {}, productId: {}", 
                    advertisementVO.getId(), advertisementVO.getProductId(), e);
            return null;
        } catch (Exception e) {
            logger.error("更新广告时发生未知错误", e);
            return null;
        }
    }

    @Override
    public String deleteAdvertisement(Integer id) {
        try {
            if (advertisementRepository.existsById(id)) {
                advertisementRepository.deleteById(id);
                logger.info("删除广告成功: id={}", id);
                return "删除成功";
            }
            logger.warn("删除广告失败: 广告不存在, id={}", id);
            return null;
        } catch (Exception e) {
            logger.error("删除广告时发生未知错误: id={}", id, e);
            return null;
        }
    }

    private AdvertisementVO convertToVO(AdvertisementPO advertisementPO) {
        return AdvertisementVO.builder()
                .id(String.valueOf(advertisementPO.getId()))
                .title(advertisementPO.getTitle())
                .content(advertisementPO.getContent())
                .imgUrl(advertisementPO.getImgUrl())
                .productId(String.valueOf(advertisementPO.getProductId()))
                .build();
    }
} 