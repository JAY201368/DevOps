package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.AdvertisementPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.repository.AdvertisementRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {
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
                System.out.printf("创建广告失败: 商品不存在, productId: %d\n", productId);
                return null; // 商品不存在，返回null
            }

            AdvertisementPO advertisementPO = AdvertisementPO.builder()
                    .title(advertisementVO.getTitle())
                    .content(advertisementVO.getContent())
                    .imgUrl(advertisementVO.getImgUrl())
                    .productId(productId.intValue())
                    .build();

            AdvertisementPO savedPO = advertisementRepository.save(advertisementPO);
            System.out.printf("创建广告成功: %s\n", savedPO);
            return convertToVO(savedPO);
        } catch (NumberFormatException e) {
            System.out.printf("创建广告失败: 商品ID格式错误: %s\n", advertisementVO.getProductId());
            return null;
        } catch (Exception e) {
            System.out.printf("创建广告时发生未知错误\n");
            e.printStackTrace();
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
                System.out.printf("更新广告失败: 广告不存在, adId: %d\n", adId);
                return null;
            }

            // 检查商品是否存在
            Long productId = Long.valueOf(advertisementVO.getProductId());
            Optional<ProductPO> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                System.out.printf("更新广告失败: 商品不存在, productId: %d\n", productId);
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
            System.out.printf("更新广告成功: %s\n", advertisementPO);
            return "更新成功";
        } catch (NumberFormatException e) {
            System.out.printf("更新广告失败: ID格式错误: adId: %s, productId: %s\n", 
                    advertisementVO.getId(), advertisementVO.getProductId());
            return null;
        } catch (Exception e) {
            System.out.printf("更新广告时发生未知错误\n");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String deleteAdvertisement(Integer id) {
        try {
            if (advertisementRepository.existsById(id)) {
                advertisementRepository.deleteById(id);
                System.out.printf("删除广告成功: id=%d\n", id);
                return "删除成功";
            }
            System.out.printf("删除广告失败: 广告不存在, id=%d\n", id);
            return null;
        } catch (Exception e) {
            System.out.printf("删除广告时发生未知错误: id=%d\n", id);
            e.printStackTrace();
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