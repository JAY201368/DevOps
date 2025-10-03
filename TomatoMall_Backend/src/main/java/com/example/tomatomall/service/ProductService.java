package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.StockpileVO;
import java.util.List;

public interface ProductService {
    List<ProductVO> getAllProducts();
    ProductVO getProductById(Long id);
    ProductVO createProduct(ProductVO productVO);
    ProductVO updateProduct(ProductVO productVO);
    ProductVO updateProductBasicInfo(ProductVO productVO);
    void deleteProduct(Long id);
    StockpileVO updateStockpile(Long productId, Integer amount);
    StockpileVO getStockpile(Long productId);
    boolean reduceStock(Long productId, Integer quantity);  // Add this method
}