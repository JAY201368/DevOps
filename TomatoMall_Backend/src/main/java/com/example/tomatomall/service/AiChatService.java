package com.example.tomatomall.service;

import java.util.List;
import java.util.Map;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    
    /**
     * 获取图书信息作为AI聊天的上下文
     * @param query 用户查询的关键词（可选）
     * @return 图书信息上下文
     */
    String getChatContext(String query);

    /**
     * 添加对话历史
     * @param userId 用户ID
     * @param question 用户问题
     * @param answer AI回答
     */
    void addConversation(String userId, String question, String answer);

    /**
     * 获取用户的对话历史
     * @param userId 用户ID
     * @return 对话历史列表
     */
    List<Map<String, String>> getConversationHistory(String userId);

    /**
     * 清除用户的对话历史
     * @param userId 用户ID
     */
    void clearConversationHistory(String userId);

    /**
     * 将对话历史转换为系统提示
     * @param userId 用户ID
     * @return 包含对话历史的系统提示
     */
    String getConversationContext(String userId);
} 