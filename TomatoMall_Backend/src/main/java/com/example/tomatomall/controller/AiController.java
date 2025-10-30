package com.example.tomatomall.controller;

import com.example.tomatomall.service.AiService;
import com.example.tomatomall.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", "400",
                    "msg", "查询内容不能为空"
                ));
            }

            List<Product> results = aiService.query(query);
            return ResponseEntity.ok(Map.of(
                "code", "200",
                "data", results
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "code", "500",
                "msg", "查询失败：" + e.getMessage()
            ));
        }
    }

    @PostMapping("/speech")
    public ResponseEntity<?> speechToText(@RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", "400",
                    "msg", "音频文件不能为空"
                ));
            }

            String text = aiService.speechToText(audioFile.getBytes());
            return ResponseEntity.ok(Map.of(
                "code", "200",
                "data", Map.of("text", text)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "code", "500",
                "msg", "语音识别失败：" + e.getMessage()
            ));
        }
    }
} 