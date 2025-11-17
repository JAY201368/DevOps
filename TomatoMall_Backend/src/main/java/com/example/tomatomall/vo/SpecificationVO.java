package com.example.tomatomall.vo;

import com.example.tomatomall.po.SpecificationPO;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;

@Data
@Setter
@Getter
public class SpecificationVO {
    private Long id;
    private String item;
    private String value;
    private Long productId;

    public static SpecificationVO fromPO(SpecificationPO specificationPO) {
        SpecificationVO specificationVO = new SpecificationVO();
        specificationVO.setId(specificationPO.getId());
        specificationVO.setItem(specificationPO.getItem());
        specificationVO.setValue(specificationPO.getValue());
        specificationVO.setProductId(specificationPO.getProduct().getId());
        return specificationVO;
    }
} 