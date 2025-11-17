package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import java.util.List;

public interface ProductService {
    List<ProductVO> getAllProducts();

    ProductVO getProductById(Long id);

    ProductVO createProduct(ProductVO productVO);

    ProductVO updateProduct(Long id, ProductVO productVO);

    void deleteProduct(Long id);

    boolean reduceStock(Long productId, Integer quantity);

    boolean restoreStock(Long productId, Integer quantity);

    StockpileVO getStockpile(Long productId);

    SpecificationVO getSpecification(Long productId);

    ProductVO updateProductBasicInfo(ProductVO productVO);

    StockpileVO updateStockpile(Long productId, Integer amount);
}