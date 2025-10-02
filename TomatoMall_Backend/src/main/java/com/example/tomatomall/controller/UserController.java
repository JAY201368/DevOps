package com.example.tomatomall.controller;

import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.UserVO;
import com.example.tomatomall.vo.ResultVO;
import com.example.tomatomall.exception.TomatoMallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResultVO<String> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String token = userService.login(loginRequest.get("username"), loginRequest.get("password"));
            return ResultVO.buildSuccess(token);
        } catch (TomatoMallException e) {
            System.out.println(e.getMessage());
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    @PostMapping
    public ResultVO<String> register(@RequestBody UserVO userVO) {
        try {
            userService.register(userVO);
            return ResultVO.buildSuccess("注册成功");
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    @GetMapping("/{username}")
    public ResultVO<UserVO> getUserInfo(@PathVariable String username, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            UserVO userVO = userService.getUserByUsername(username);
            return ResultVO.buildSuccess(userVO);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    @PutMapping
    public ResultVO<String> updateUser(@RequestBody UserVO userVO, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResultVO.buildFailure("未授权", "401");
            }
            userService.updateUser(userVO);
            return ResultVO.buildSuccess("更新成功");
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
}