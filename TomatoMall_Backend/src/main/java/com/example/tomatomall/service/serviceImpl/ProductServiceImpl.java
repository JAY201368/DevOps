package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.CommentPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.SpecificationPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import com.example.tomatomall.exception.TomatoMallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<ProductVO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductVO productVO = ProductVO.fromPO(product);
                    // 计算基于评论的平均评分
                    List<CommentPO> comments = commentRepository.findByProductIdAndStatus(product.getId(), 1, Pageable.unpaged()).getContent();
                    if (!comments.isEmpty()) {
                        double averageRating = comments.stream()
                            .mapToDouble(CommentPO::getRating)
                            .average()
                            .orElse(0.0);
                        // 直接使用5分制的评分
                        productVO.setRate(averageRating);
                    } else {
                        // 如果没有评论，设置为null，前端会显示"暂无评分"
                        productVO.setRate(null);
                    }
                    return productVO;
                })
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
    public ProductVO updateProduct(Long id, ProductVO productVO) {
        ProductPO existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));

        updateProductFromVO(existingProduct, productVO);
        ProductPO updatedProduct = productRepository.save(existingProduct);
        return ProductVO.fromPO(updatedProduct);
    }

    @Override
    @Transactional
    public ProductVO updateProductBasicInfo(ProductVO productVO) {
        // 获取现有商品
        ProductPO existingProduct = productRepository.findById(productVO.getId())
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));

        // 仅更新基本字段，不处理rate字段（由评论系统自动维护）
        existingProduct.setTitle(productVO.getTitle());
        existingProduct.setPrice(productVO.getPrice());
        // 不更新rate字段，保持评论系统计算的值
        existingProduct.setDescription(productVO.getDescription());
        existingProduct.setCover(productVO.getCover());
        existingProduct.setDetail(productVO.getDetail());

        // 保存并返回更新后的商品
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

    @Override
    @Transactional
    public boolean reduceStock(Long productId, Integer quantity) {
        ProductPO product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));

        StockpilePO stockpile = product.getStockpile();
        if (stockpile == null || stockpile.getAmount() < quantity) {
            throw new TomatoMallException(400, "商品库存不足");
        }

        // 使用乐观锁更新库存
        stockpile.setAmount(stockpile.getAmount() - quantity);

        try {
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean restoreStock(Long productId, Integer quantity) {
        try {
            ProductPO product = productRepository.findById(productId)
                    .orElseThrow(() -> new TomatoMallException(404, "商品不存在"));

            StockpilePO stockpile = product.getStockpile();
            if (stockpile == null) {
                stockpile = new StockpilePO();
                stockpile.setProduct(product);
                product.setStockpile(stockpile);
            }

            stockpile.setAmount(stockpile.getAmount() + quantity);
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SpecificationVO getSpecification(Long productId) {
        ProductPO product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在"));

        return SpecificationVO.fromPO(product.getSpecification());
    }

    private void updateProductFromVO(ProductPO productPO, ProductVO productVO) {
        productPO.setTitle(productVO.getTitle());
        productPO.setPrice(productVO.getPrice());
        // rate字段由评论系统自动维护，不允许手动更新
        // 对于新商品，rate应该设置为null，由评论系统自动计算（5分制）
        if (productPO.getId() == null) {
            // 新商品，设置rate为null
            productPO.setRate(null);
        }
        // 对于编辑现有商品，不修改rate字段，保持评论系统计算的值
        productPO.setDescription(productVO.getDescription());
        productPO.setCover(productVO.getCover());
        productPO.setDetail(productVO.getDetail());
        productPO.setTags(productVO.getTags());

        if (productVO.getSpecifications() != null) {
            // 如果是更新操作（已有ID），而不是新建操作
            if (productPO.getId() != null) {
                // 获取现有规格集合（如果为null则初始化一个空集合）
                if (productPO.getSpecifications() == null) {
                    productPO.setSpecifications(new java.util.HashSet<>());
                }

                // 保存已经存在的规格项
                java.util.Map<String, SpecificationPO> existingSpecs = new java.util.HashMap<>();
                for (SpecificationPO spec : productPO.getSpecifications()) {
                    existingSpecs.put(spec.getItem(), spec);
                }

                // 使用新值更新或创建规格项
                java.util.Set<SpecificationPO> updatedSpecs = new java.util.HashSet<>();
                for (SpecificationVO specVO : productVO.getSpecifications()) {
                    // 如果规格项已存在，更新其值
                    if (existingSpecs.containsKey(specVO.getItem())) {
                        SpecificationPO existingSpec = existingSpecs.get(specVO.getItem());
                        existingSpec.setValue(specVO.getValue());
                        updatedSpecs.add(existingSpec);
                    } else {
                        // 如果规格项不存在，创建新的
                        SpecificationPO newSpec = new SpecificationPO();
                        newSpec.setItem(specVO.getItem());
                        newSpec.setValue(specVO.getValue());
                        newSpec.setProduct(productPO);
                        updatedSpecs.add(newSpec);
                    }
                }

                // 更新商品的规格集合 - 使用clear()和addAll()而不是直接替换集合
                productPO.getSpecifications().clear();
                productPO.getSpecifications().addAll(updatedSpecs);
            } else {
                // 对于新商品，直接创建所有规格项
                java.util.Set<SpecificationPO> newSpecs = productVO.getSpecifications().stream()
                        .map(specVO -> {
                            SpecificationPO specPO = new SpecificationPO();
                            specPO.setItem(specVO.getItem());
                            specPO.setValue(specVO.getValue());
                            specPO.setProduct(productPO);
                            return specPO;
                        })
                        .collect(Collectors.toSet());
                productPO.setSpecifications(newSpecs);
            }
        } else if (productPO.getId() != null && productPO.getSpecifications() != null) {
            // 如果传入的规格为null但实体已存在，清空现有规格而不是设置为null
            productPO.getSpecifications().clear();
        }
    }
}