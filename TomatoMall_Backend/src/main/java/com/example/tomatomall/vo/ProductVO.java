package com.example.tomatomall.vo;

import com.example.tomatomall.po.ProductPO;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Setter
@Getter
public class ProductVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private Double rate;
    private String description;
    private String cover;
    private String detail;
    private Set<SpecificationVO> specifications;

    public static ProductVO fromPO(ProductPO productPO) {
        ProductVO productVO = new ProductVO();
        productVO.setId(productPO.getId());
        productVO.setTitle(productPO.getTitle());
        productVO.setPrice(productPO.getPrice());
        productVO.setRate(productPO.getRate());
        productVO.setDescription(productPO.getDescription());
        productVO.setCover(productPO.getCover());
        productVO.setDetail(productPO.getDetail());
        if (productPO.getSpecifications() != null) {
            productVO.setSpecifications(productPO.getSpecifications().stream()
                    .map(SpecificationVO::fromPO)
                    .collect(Collectors.toSet()));
        }
        return productVO;
    }
} 