package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private BCryptPasswordEncoder passwordEncoder;
    private UserPO testUser;
    private UserVO testUserVO;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        testUser = new UserPO();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setTelephone("13800138000");
        testUser.setRole("user");
        testUser.setLocation("Test Location");
        testUser.setAvatar("avatar.jpg");

        testUserVO = new UserVO();
        testUserVO.setUsername("testuser");
        testUserVO.setPassword("password123");
        testUserVO.setName("Test User");
        testUserVO.setEmail("test@example.com");
        testUserVO.setTelephone("13800138000");
        testUserVO.setRole("user");
        testUserVO.setLocation("Test Location");
        testUserVO.setAvatar("avatar.jpg");
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("testuser")).thenReturn("mock-jwt-token");

        String token = userService.login("testuser", "password123");

        assertNotNull(token);
        assertEquals("mock-jwt-token", token);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void testLogin_UsernameNull() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.login(null, "password123");
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testLogin_UsernameEmpty() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.login("", "password123");
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testLogin_PasswordNull() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.login("testuser", null);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testLogin_PasswordEmpty() {
        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.login("testuser", "");
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testLogin_UserNotExists() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.login("nonexistent", "password123");
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(UserPO.class))).thenAnswer(invocation -> {
            UserPO savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        UserVO newUser = new UserVO();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setName("New User");
        newUser.setTelephone("13900139000");
        newUser.setEmail("newuser@example.com");
        newUser.setRole("user");

        UserVO result = userService.register(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("New User", result.getName());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).save(any(UserPO.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.register(testUserVO);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testRegister_InvalidPhoneNumber() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        UserVO newUser = new UserVO();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setName("New User");
        newUser.setTelephone("12345");
        newUser.setEmail("newuser@example.com");
        newUser.setRole("user");

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            userService.register(newUser);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testRegister_ValidPhoneNumber() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(UserPO.class))).thenAnswer(invocation -> {
            UserPO savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        UserVO newUser = new UserVO();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setName("New User");
        newUser.setTelephone("13912345678");
        newUser.setEmail("newuser@example.com");
        newUser.setRole("user");

        UserVO result = userService.register(newUser);

        assertNotNull(result);
        assertEquals("13912345678", result.getTelephone());
    }

    @Test
    void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserVO result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getName());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPO.class))).thenReturn(testUser);

        UserVO updateVO = new UserVO();
        updateVO.setUsername("testuser");
        updateVO.setName("Updated Name");
        updateVO.setEmail("updated@example.com");

        UserVO result = userService.updateUser(updateVO);

        assertNotNull(result);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(UserPO.class));
    }

    @Test
    void testUpdateUser_WithPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPO.class))).thenReturn(testUser);

        UserVO updateVO = new UserVO();
        updateVO.setUsername("testuser");
        updateVO.setPassword("newpassword123");

        UserVO result = userService.updateUser(updateVO);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(UserPO.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UserVO updateVO = new UserVO();
        updateVO.setUsername("nonexistent");

        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUser(updateVO);
        });
    }

    @Test
    void testGetUserIdByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Long userId = userService.getUserIdByUsername("testuser");

        assertNotNull(userId);
        assertEquals(1L, userId);
    }

    @Test
    void testGetUserIdByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Long userId = userService.getUserIdByUsername("nonexistent");

        assertNull(userId);
    }

    @Test
    void testGetAllUsers() {
        UserPO user1 = new UserPO();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setName("User One");

        UserPO user2 = new UserPO();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setName("User Two");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserVO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }
}
