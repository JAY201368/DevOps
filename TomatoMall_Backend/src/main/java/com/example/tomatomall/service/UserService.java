package com.example.tomatomall.service;

import com.example.tomatomall.vo.UserVO;
import java.util.List;

public interface UserService {
    String login(String username, String password);
    UserVO register(UserVO userVO);
    UserVO getUserByUsername(String username);
    UserVO updateUser(UserVO userVO);
    Long getUserIdByUsername(String username);
    List<UserVO> getAllUsers();
} 