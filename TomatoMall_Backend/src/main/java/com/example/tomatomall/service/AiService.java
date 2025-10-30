package com.example.tomatomall.service;

import com.example.tomatomall.model.Product;
import java.util.List;

public interface AiService {
    List<Product> query(String query);
    String speechToText(byte[] audioData);
} 