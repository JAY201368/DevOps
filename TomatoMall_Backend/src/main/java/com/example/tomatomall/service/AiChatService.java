package com.example.tomatomall.service;

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
} 