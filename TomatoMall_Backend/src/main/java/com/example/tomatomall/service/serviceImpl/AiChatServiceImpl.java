package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public String getChatContext(String query) {
        // 获取所有图书信息
        List<ProductPO> allProducts = productRepository.findAll();
        
        // 构建上下文信息
        StringBuilder context = new StringBuilder();
        context.append("以下是番茄书城的图书信息，可以作为回答用户问题的参考：\n\n");
        
        // 添加图书信息
        for (ProductPO product : allProducts) {
            context.append(String.format("书名：%s\n", product.getTitle()));
            context.append(String.format("描述：%s\n", product.getDescription()));
            if (product.getTags() != null && !product.getTags().isEmpty()) {
                context.append(String.format("标签：%s\n", product.getTags()));
            }
            if (product.getRate() != null) {
                context.append(String.format("评分：%.1f\n", product.getRate()));
            }
            context.append("\n");
        }
        
        // 添加系统提示
        context.append("\n请基于以上图书信息，回答用户的问题。如果用户的问题与图书无关，请礼貌地告知用户您只能回答与图书相关的问题。");
        
        return context.toString();
    }
} 