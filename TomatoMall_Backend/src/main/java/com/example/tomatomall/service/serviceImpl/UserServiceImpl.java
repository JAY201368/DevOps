package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.UserVO;
import com.example.tomatomall.exception.TomatoMallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern PHONE_PATTERN = Pattern
            .compile("^1[3-9]\\d{9}$");

    @Override
    public String login(String username, String password) {
        try {
            logger.debug("开始验证用户: {}", username);

            if (username == null || username.trim().isEmpty()) {
                logger.error("登录失败: 用户名为空");
                throw TomatoMallException.userNotExists();
            }

            if (password == null || password.trim().isEmpty()) {
                logger.error("登录失败: 密码为空");
                throw TomatoMallException.passwordError();
            }

            Optional<UserPO> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                logger.error("登录失败: 用户不存在, 用户名: {}", username);
                throw TomatoMallException.userNotExists();
            }

            UserPO userPO = userOpt.get();
            logger.debug("获取到用户信息: {}", userPO.getUsername());

            if (!passwordEncoder.matches(password, userPO.getPassword())) {
                logger.error("登录失败: 密码不匹配, 用户名: {}", username);
                throw TomatoMallException.nameOrPasswordError();
            }

            logger.debug("密码验证成功, 开始生成token");
            String token = jwtUtil.generateToken(username);
            logger.debug("token生成成功");
            return token;
        } catch (TomatoMallException e) {
            logger.error("登录业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("登录系统异常: {}", e.getMessage(), e);
            throw new TomatoMallException(500, "登录处理失败: " + e.getMessage());
        }
    }

    @Override
    public UserVO register(UserVO userVO) {
        if (userRepository.existsByUsername(userVO.getUsername())) {
            throw TomatoMallException.userAlreadyExists();
        }

        // 验证手机号格式
        if (userVO.getTelephone() != null && !userVO.getTelephone().isEmpty() &&
                !PHONE_PATTERN.matcher(userVO.getTelephone()).matches()) {
            throw TomatoMallException.invalidPhoneNumber();
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
        if (userVO.getName() != null)
            existingUser.setName(userVO.getName());
        if (userVO.getAvatar() != null)
            existingUser.setAvatar(userVO.getAvatar());
        if (userVO.getRole() != null)
            existingUser.setRole(userVO.getRole());
        if (userVO.getTelephone() != null)
            existingUser.setTelephone(userVO.getTelephone());
        if (userVO.getEmail() != null)
            existingUser.setEmail(userVO.getEmail());
        if (userVO.getLocation() != null)
            existingUser.setLocation(userVO.getLocation());

        UserPO updatedUser = userRepository.save(existingUser);
        return UserVO.fromPO(updatedUser);
    }
}