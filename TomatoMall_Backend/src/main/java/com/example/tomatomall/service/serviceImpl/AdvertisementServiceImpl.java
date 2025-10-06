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
        // 检查商品是否存在
        Long productId = Long.valueOf(advertisementVO.getProductId());
        Optional<ProductPO> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return null; // 商品不存在，返回null
        }

        AdvertisementPO advertisementPO = AdvertisementPO.builder()
                .title(advertisementVO.getTitle())
                .content(advertisementVO.getContent())
                .imgUrl(advertisementVO.getImgUrl())
                .productId(productId.intValue())
                .build();

        AdvertisementPO savedPO = advertisementRepository.save(advertisementPO);
        return convertToVO(savedPO);
    }

    @Override
    public String updateAdvertisement(AdvertisementVO advertisementVO) {
        // 检查广告是否存在
        Integer adId = Integer.valueOf(advertisementVO.getId());
        Optional<AdvertisementPO> advertisementOpt = advertisementRepository.findById(adId);
        if (!advertisementOpt.isPresent()) {
            return null;
        }

        // 检查商品是否存在
        Long productId = Long.valueOf(advertisementVO.getProductId());
        Optional<ProductPO> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
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
        return "更新成功";
    }

    @Override
    public String deleteAdvertisement(Integer id) {
        if (advertisementRepository.existsById(id)) {
            advertisementRepository.deleteById(id);
            return "删除成功";
        }
        return null;
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