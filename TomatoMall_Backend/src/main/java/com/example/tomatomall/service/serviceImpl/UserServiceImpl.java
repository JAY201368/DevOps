package com.example.tomatomall.service.impl;

import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        Optional<UserPO> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            throw new RuntimeException("用户不存在或密码错误");
        }
        return jwtUtil.generateToken(username);
    }

    @Override
    public UserVO register(UserVO userVO) {
        if (userRepository.existsByUsername(userVO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        UserPO userPO = new UserPO();
        userPO.setUsername(userVO.getUsername());
        userPO.setPassword(passwordEncoder.encode(userVO.getPassword()));
        userPO.setName(userVO.getName());
        userPO.setAvatar(userVO.getAvatar());
        userPO.setRole(userVO.getRole());
        userPO.setTelephone(userVO.getTelephone());
        userPO.setEmail(userVO.getEmail());
        userPO.setLocation(userVO.getLocation());
        
        UserPO savedUser = userRepository.save(userPO);
        return UserVO.fromPO(savedUser);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        UserPO userPO = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        return UserVO.fromPO(userPO);
    }

    @Override
    public UserVO updateUser(UserVO userVO) {
        UserPO existingUser = userRepository.findByUsername(userVO.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        if (userVO.getPassword() != null && !userVO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userVO.getPassword()));
        }
        if (userVO.getName() != null) existingUser.setName(userVO.getName());
        if (userVO.getAvatar() != null) existingUser.setAvatar(userVO.getAvatar());
        if (userVO.getRole() != null) existingUser.setRole(userVO.getRole());
        if (userVO.getTelephone() != null) existingUser.setTelephone(userVO.getTelephone());
        if (userVO.getEmail() != null) existingUser.setEmail(userVO.getEmail());
        if (userVO.getLocation() != null) existingUser.setLocation(userVO.getLocation());
        
        UserPO updatedUser = userRepository.save(existingUser);
        return UserVO.fromPO(updatedUser);
    }
} 