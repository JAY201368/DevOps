package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private ProductRepository productRepository;

    // 使用ConcurrentHashMap存储每个用户的对话历史
    private final Map<String, Queue<Map<String, String>>> userConversations = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 5;

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
        context.append("\n请基于以上图书信息，回答用户的问题。");
        
        return context.toString();
    }

    /**
     * 添加对话历史
     * @param userId 用户ID
     * @param question 用户问题
     * @param answer AI回答
     */
    public void addConversation(String userId, String question, String answer) {
        Queue<Map<String, String>> history = userConversations.computeIfAbsent(userId, k -> new LinkedList<>());
        
        Map<String, String> conversation = new HashMap<>();
        conversation.put("question", question);
        conversation.put("answer", answer);
        
        // 如果队列已满，移除最旧的对话
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.poll();
        }
        
        history.offer(conversation);
    }

    /**
     * 获取用户的对话历史
     * @param userId 用户ID
     * @return 对话历史列表
     */
    public List<Map<String, String>> getConversationHistory(String userId) {
        Queue<Map<String, String>> history = userConversations.get(userId);
        if (history == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    /**
     * 清除用户的对话历史
     * @param userId 用户ID
     */
    public void clearConversationHistory(String userId) {
        userConversations.remove(userId);
    }

    /**
     * 将对话历史转换为系统提示
     * @param userId 用户ID
     * @return 包含对话历史的系统提示
     */
    public String getConversationContext(String userId) {
        List<Map<String, String>> history = getConversationHistory(userId);
        if (history.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n\n以下是之前的对话历史，请参考这些上下文来回答用户的问题：\n");
        for (Map<String, String> conversation : history) {
            context.append("\n用户：").append(conversation.get("question"));
            context.append("\nAI：").append(conversation.get("answer"));
        }
        return context.toString();
    }
} 