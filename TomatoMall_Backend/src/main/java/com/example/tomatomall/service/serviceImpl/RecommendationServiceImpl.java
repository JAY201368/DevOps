package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.OrderItemPO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.po.WishListItemPO;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.repository.WishListItemRepository;
import com.example.tomatomall.service.RecommendationService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockpileRepository stockpileRepository;
    
    @Autowired
    private WishListItemRepository wishListItemRepository;

    // 辅助方法：处理标签字符串，支持中英文逗号
    private String[] processTagString(String tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        // 将中文逗号替换为英文逗号，然后按逗号分隔
        return tags.replace("，", ",").split(",");
    }

    @Override
    public List<ProductVO> getPersonalizedRecommendations(Long userId, int limit) {
        // 1. 获取用户的历史订单
        List<OrderPO> userOrders = orderRepository.findByUserId(userId);
        
        // 如果用户没有购买记录，返回热门推荐
        if (userOrders == null || userOrders.isEmpty()) {
            return getPopularRecommendations(limit);
        }
        
        // 2. 分析用户购买的书籍标签分布
        Map<String, Integer> tagFrequency = new HashMap<>();
        int totalTags = 0;
        
        for (OrderPO order : userOrders) {
            // 只考虑已完成的订单
            if (!"completed".equalsIgnoreCase(order.getStatus())) {
                continue;
            }
            
            for (OrderItemPO item : order.getOrderItems()) {
                Long productId = item.getProductId();
                Optional<ProductPO> productOpt = productRepository.findById(productId);
                
                if (productOpt.isPresent()) {
                    ProductPO product = productOpt.get();
                    String tags = product.getTags();
                    
                    if (tags != null && !tags.isEmpty()) {
                        // 使用辅助方法处理标签
                        String[] tagArray = processTagString(tags);
                        for (String tag : tagArray) {
                            tag = tag.trim();
                            if (!tag.isEmpty()) {
                                tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                                totalTags++;
                            }
                        }
                    }
                }
            }
        }
        
        // 如果没有提取到任何标签，返回热门推荐
        if (tagFrequency.isEmpty()) {
            return getPopularRecommendations(limit);
        }
        
        // 3. 计算每个标签的权重
        Map<String, Double> tagWeights = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
            double weight = (double) entry.getValue() / totalTags;
            tagWeights.put(entry.getKey(), weight);
        }
        
        // 4. 按权重排序标签
        List<Map.Entry<String, Double>> sortedTags = new ArrayList<>(tagWeights.entrySet());
        sortedTags.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        
        // 5. 获取用户已购买的商品ID列表（用于排除）
        Set<Long> purchasedProductIds = new HashSet<>();
        for (OrderPO order : userOrders) {
            for (OrderItemPO item : order.getOrderItems()) {
                purchasedProductIds.add(item.getProductId());
            }
        }
        
        // 6. 基于标签权重推荐书籍
        List<ProductPO> allProducts = productRepository.findAll();
        Map<Long, Double> productScores = new HashMap<>();
        
        // 获取用户愿望单中的图书ID列表
        List<WishListItemPO> wishListItems = wishListItemRepository.findByUserId(userId);
        Set<Long> wishListProductIds = new HashSet<>();
        for (WishListItemPO item : wishListItems) {
            wishListProductIds.add(item.getBookId());
        }
        
        for (ProductPO product : allProducts) {
            // 跳过用户已购买的商品
            if (purchasedProductIds.contains(product.getId())) {
                continue;
            }
            
            // 检查库存状态，跳过库存不足的商品
            Optional<StockpilePO> stockpileOpt = stockpileRepository.findByProductId(product.getId());
            boolean hasStock = stockpileOpt.isPresent() && stockpileOpt.get().getAmount() > 0;
            if (!hasStock) {
                continue;
            }
            
            String productTags = product.getTags();
            if (productTags == null || productTags.isEmpty()) {
                continue;
            }
            
            // 计算商品得分（基于标签匹配和其他因素）
            double score = 0.0;
            // 使用辅助方法处理标签
            String[] productTagArray = processTagString(productTags);
            
            // 记录匹配的标签
            Set<String> matchedTags = new HashSet<>();
            
            for (String tag : productTagArray) {
                tag = tag.trim();
                if (tagWeights.containsKey(tag)) {
                    score += tagWeights.get(tag);
                    matchedTags.add(tag);
                }
            }
            
            // 如果没有匹配的标签，跳过
            if (matchedTags.isEmpty()) {
                continue;
            }
            
            // 添加标签匹配数量的加权
            score = score * (1.0 + 0.3 * matchedTags.size());
            
            // 考虑评分因素（现在产品的rate字段是5分制）
            if (product.getRate() != null) {
                score *= (0.5 + product.getRate() / 10.0); // 评分影响因子，5分制
            }
            
            // 如果是愿望单中的商品，增加其得分
            if (wishListProductIds.contains(product.getId())) {
                score *= 1.5; // 愿望单中的商品得分提升50%
            }
            
            productScores.put(product.getId(), score);
        }
        
        // 7. 按得分排序并选出前limit个
        List<Long> recommendedProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 8. 获取推荐商品详情
        List<ProductVO> recommendations = new ArrayList<>();
        for (Long productId : recommendedProductIds) {
            productRepository.findById(productId).ifPresent(product -> 
                recommendations.add(ProductVO.fromPO(product))
            );
        }
        
        return recommendations;
    }

    @Override
    public List<ProductVO> getPopularRecommendations(int limit) {
        // 限制最多返回9个热门推荐商品
        int actualLimit = Math.min(limit, 9);
        
        // 获取评分最高的书籍，过滤库存不足的书籍
        List<ProductPO> allProducts = productRepository.findAll();
        
        // 为每个产品计算得分，考虑评分和库存状态
        Map<Long, Double> productScores = new HashMap<>();
        for (ProductPO product : allProducts) {
            // 检查库存状态，跳过库存不足的商品
            Optional<StockpilePO> stockpileOpt = stockpileRepository.findByProductId(product.getId());
            boolean hasStock = stockpileOpt.isPresent() && stockpileOpt.get().getAmount() > 0;
            if (!hasStock) {
                continue;
            }
            
            double score = 0.0;
            
            // 基础得分主要由评分决定
            if (product.getRate() != null) {
                score = product.getRate(); // 基础得分就是评分（5分制）
            }
            
            productScores.put(product.getId(), score);
        }
        
        // 按得分排序并选出前actualLimit个
        List<Long> recommendedProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(actualLimit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 获取推荐商品详情
        List<ProductVO> recommendations = new ArrayList<>();
        for (Long productId : recommendedProductIds) {
            productRepository.findById(productId).ifPresent(product -> 
                recommendations.add(ProductVO.fromPO(product))
            );
        }
        
        return recommendations;
    }

    @Override
    public List<ProductVO> getWishListBasedRecommendations(Long userId, int limit) {
        // 1. 获取用户的愿望单
        List<WishListItemPO> wishListItems = wishListItemRepository.findByUserId(userId);
        
        // 如果用户没有愿望单记录，返回热门推荐
        if (wishListItems == null || wishListItems.isEmpty()) {
            return getPopularRecommendations(limit);
        }
        
        // 2. 分析用户愿望单中的书籍标签分布
        Map<String, Integer> tagFrequency = new HashMap<>();
        int totalTags = 0;
        Set<Long> wishListProductIds = new HashSet<>();
        
        for (WishListItemPO item : wishListItems) {
            Long bookId = item.getBookId();
            wishListProductIds.add(bookId);
            
            Optional<ProductPO> productOpt = productRepository.findById(bookId);
            if (productOpt.isPresent()) {
                ProductPO product = productOpt.get();
                String tags = product.getTags();
                
                if (tags != null && !tags.isEmpty()) {
                    // 使用辅助方法处理标签
                    String[] tagArray = processTagString(tags);
                    for (String tag : tagArray) {
                        tag = tag.trim();
                        if (!tag.isEmpty()) {
                            tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                            totalTags++;
                        }
                    }
                }
            }
        }
        
        // 如果没有提取到任何标签，返回热门推荐
        if (tagFrequency.isEmpty()) {
            return getPopularRecommendations(limit);
        }
        
        // 记录愿望单标签分布情况，帮助调试
        System.out.println("用户ID: " + userId + " 的愿望单标签分布:");
        for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
            System.out.println("标签: " + entry.getKey() + ", 频率: " + entry.getValue());
        }
        
        // 3. 计算每个标签的权重
        Map<String, Double> tagWeights = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
            double weight = (double) entry.getValue() / totalTags;
            // 增加权重值，使标签匹配更加明显
            weight = weight * 3.0; // 放大权重
            tagWeights.put(entry.getKey(), weight);
        }
        
        // 4. 按权重排序标签
        List<Map.Entry<String, Double>> sortedTags = new ArrayList<>(tagWeights.entrySet());
        sortedTags.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        
        // 输出排序后的标签信息，帮助调试
        System.out.println("按权重排序的标签:");
        for (Map.Entry<String, Double> entry : sortedTags) {
            System.out.println("标签: " + entry.getKey() + ", 权重: " + entry.getValue());
        }
        
        // 5. 基于标签权重推荐书籍
        List<ProductPO> allProducts = productRepository.findAll();
        Map<Long, Double> productScores = new HashMap<>();
        Map<Long, Set<String>> productMatchedTags = new HashMap<>(); // 记录每个产品匹配的标签
        
        for (ProductPO product : allProducts) {
            // 不再跳过愿望单已有的商品
            boolean isInWishList = wishListProductIds.contains(product.getId());
            
            // 检查库存状态，跳过库存不足的商品
            Optional<StockpilePO> stockpileOpt = stockpileRepository.findByProductId(product.getId());
            boolean hasStock = stockpileOpt.isPresent() && stockpileOpt.get().getAmount() > 0;
            if (!hasStock) {
                continue;
            }
            
            String productTags = product.getTags();
            if (productTags == null || productTags.isEmpty()) {
                continue;
            }
            
            // 计算商品得分（基于标签匹配和其他因素）
            double score = 0.0;
            // 使用辅助方法处理标签
            String[] productTagArray = processTagString(productTags);
            Set<String> matchedTags = new HashSet<>();
            
            for (String tag : productTagArray) {
                tag = tag.trim();
                if (tagWeights.containsKey(tag)) {
                    score += tagWeights.get(tag);
                    matchedTags.add(tag);
                }
            }
            
            // 必须至少匹配一个标签，才有机会被推荐
            if (matchedTags.isEmpty()) {
                continue;
            }
            
            // 添加标签匹配数量的加权
            score = score * (1.0 + 0.5 * matchedTags.size());
            
            // 考虑评分因素
            if (product.getRate() != null) {
                score *= (0.5 + product.getRate() / 10.0); // 评分影响因子，5分制
            }
            
            // 如果是愿望单中的商品，调整得分
            if (isInWishList) {
                // 愿望单中的商品得分提升，确保它们也会被推荐
                score *= 1.2;
            }
            
            productScores.put(product.getId(), score);
            productMatchedTags.put(product.getId(), matchedTags);
        }
        
        // 输出匹配结果，帮助调试
        System.out.println("产品匹配结果:");
        for (Map.Entry<Long, Double> entry : productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList())) {
            Long productId = entry.getKey();
            Optional<ProductPO> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                System.out.println("产品ID: " + productId + 
                                 ", 标题: " + productOpt.get().getTitle() + 
                                 ", 得分: " + entry.getValue() +
                                 ", 匹配标签: " + String.join(", ", productMatchedTags.get(productId)) +
                                 ", 是否在愿望单: " + (wishListProductIds.contains(productId) ? "是" : "否") +
                                 ", 库存状态: " + (stockpileRepository.findByProductId(productId).isPresent() && 
                                               stockpileRepository.findByProductId(productId).get().getAmount() > 0 ? "充足" : "不足"));
            }
        }
        
        // 6. 按得分排序并选出前limit个
        List<Long> recommendedProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 7. 获取推荐商品详情
        List<ProductVO> recommendations = new ArrayList<>();
        for (Long productId : recommendedProductIds) {
            productRepository.findById(productId).ifPresent(product -> 
                recommendations.add(ProductVO.fromPO(product))
            );
        }
        
        return recommendations;
    }
} 