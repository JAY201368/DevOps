package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.BannerPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.repository.BannerRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.BannerService;
import com.example.tomatomall.vo.BannerVO;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<BannerVO> getAllBanners() {
        List<BannerPO> banners = bannerRepository.findAllByOrderByDisplayOrderAsc();
        return banners.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public BannerVO getBannerById(Long id) {
        Optional<BannerPO> bannerOpt = bannerRepository.findById(id);
        return bannerOpt.map(this::convertToVO).orElse(null);
    }

    @Override
    public BannerVO createBanner(BannerPO bannerPO) {
        // 设置默认顺序（如果未提供）
        if (bannerPO.getDisplayOrder() == null) {
            Long count = bannerRepository.count();
            bannerPO.setDisplayOrder(count.intValue() + 1);
        }
        
        BannerPO saved = bannerRepository.save(bannerPO);
        return convertToVO(saved);
    }

    @Override
    public BannerVO updateBanner(Long id, BannerPO bannerPO) {
        if (!bannerRepository.existsById(id)) {
            return null;
        }
        
        bannerPO.setId(id);
        BannerPO updated = bannerRepository.save(bannerPO);
        return convertToVO(updated);
    }

    @Override
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }

    @Override
    public BannerVO updateBannerBooks(Long id, List<Long> productIds) {
        Optional<BannerPO> bannerOpt = bannerRepository.findById(id);
        if (bannerOpt.isEmpty()) {
            return null;
        }
        
        BannerPO banner = bannerOpt.get();
        String productIdsStr = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        
        banner.setProductIds(productIdsStr);
        BannerPO updated = bannerRepository.save(banner);
        return convertToVO(updated);
    }
    
    // 将PO转换为VO，并加载关联的书籍数据
    private BannerVO convertToVO(BannerPO bannerPO) {
        BannerVO vo = BannerVO.fromPO(bannerPO);
        
        // 加载关联的书籍
        List<ProductVO> books = new ArrayList<>();
        String productIdsStr = bannerPO.getProductIds();
        
        if (productIdsStr != null && !productIdsStr.isEmpty()) {
            List<Long> productIds = Arrays.stream(productIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            for (Long productId : productIds) {
                Optional<ProductPO> productOpt = productRepository.findById(productId);
                productOpt.ifPresent(product -> books.add(ProductVO.fromPO(product)));
            }
        }
        
        vo.setBooks(books);
        return vo;
    }
} 