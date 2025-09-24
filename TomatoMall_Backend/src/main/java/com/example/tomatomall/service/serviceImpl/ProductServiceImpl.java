package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.SpecificationPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import com.example.tomatomall.exception.TomatoMallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductVO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductVO::fromPO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVO getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductVO::fromPO)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
    }

    @Override
    @Transactional
    public ProductVO createProduct(ProductVO productVO) {
        System.out.println("开始创建商品: " + productVO.getTitle());
        
        ProductPO productPO = new ProductPO();
        updateProductFromVO(productPO, productVO);
        
        // 创建库存
        StockpilePO stockpilePO = new StockpilePO();
        stockpilePO.setAmount(0);
        stockpilePO.setFrozen(0);
        stockpilePO.setProduct(productPO);
        productPO.setStockpile(stockpilePO);

        try {
            ProductPO savedProduct = productRepository.save(productPO);
            System.out.println("商品创建成功: " + savedProduct.getId());
            return ProductVO.fromPO(savedProduct);
        } catch (Exception e) {
            System.err.println("创建商品时出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductVO updateProduct(ProductVO productVO) {
        ProductPO existingProduct = productRepository.findById(productVO.getId())
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
        
        updateProductFromVO(existingProduct, productVO);
        ProductPO updatedProduct = productRepository.save(existingProduct);
        return ProductVO.fromPO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("商品不存在");
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public StockpileVO updateStockpile(Long productId, Integer amount) {
        ProductPO product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
        
        StockpilePO stockpile = product.getStockpile();
        if (stockpile == null) {
            stockpile = new StockpilePO();
            stockpile.setProduct(product);
            product.setStockpile(stockpile);
        }
        
        stockpile.setAmount(amount);
        stockpile.setFrozen(0);
        
        ProductPO savedProduct = productRepository.save(product);
        return StockpileVO.fromPO(savedProduct.getStockpile());
    }

    @Override
    public StockpileVO getStockpile(Long productId) {
        ProductPO product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
        
        StockpilePO stockpile = product.getStockpile();
        if (stockpile == null) {
            throw TomatoMallException.productNotExists();
        }
        
        return StockpileVO.fromPO(stockpile);
    }

    private void updateProductFromVO(ProductPO productPO, ProductVO productVO) {
        productPO.setTitle(productVO.getTitle());
        productPO.setPrice(productVO.getPrice());
        productPO.setRate(productVO.getRate());
        productPO.setDescription(productVO.getDescription());
        productPO.setCover(productVO.getCover());
        productPO.setDetail(productVO.getDetail());

        if (productVO.getSpecifications() != null) {
            productPO.setSpecifications(productVO.getSpecifications().stream()
                    .map(specVO -> {
                        SpecificationPO specPO = new SpecificationPO();
                        specPO.setItem(specVO.getItem());
                        specPO.setValue(specVO.getValue());
                        specPO.setProduct(productPO);
                        return specPO;
                    })
                    .collect(Collectors.toSet()));
        }
    }
} 