package com.example.tomatomall.controller;

import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String token = userService.login(loginRequest.get("username"), loginRequest.get("password"));
            Map<String, String> response = new HashMap<>();
            response.put("code", "200");
            response.put("msg", null);
            response.put("data", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", "400");
            response.put("msg", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserVO userVO) {
        try {
            userService.register(userVO);
            Map<String, String> response = new HashMap<>();
            response.put("code", "200");
            response.put("msg", null);
            response.put("data", "注册成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", "400");
            response.put("msg", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }
            UserVO userVO = userService.getUserByUsername(username);
            Map<String, Object> response = new HashMap<>();
            response.put("code", "200");
            response.put("msg", null);
            response.put("data", userVO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", "400");
            response.put("msg", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserVO userVO, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }
            userService.updateUser(userVO);
            Map<String, String> response = new HashMap<>();
            response.put("code", "200");
            response.put("msg", null);
            response.put("data", "更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", "400");
            response.put("msg", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
} 