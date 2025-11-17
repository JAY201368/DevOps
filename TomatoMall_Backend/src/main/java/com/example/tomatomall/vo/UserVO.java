package com.example.tomatomall.vo;

import com.example.tomatomall.po.UserPO;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Setter
@Getter
public class UserVO {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String avatar;

    @NotBlank(message = "角色不能为空")
    private String role;

    @Pattern(regexp = "^1[0-9]{10}$", message = "手机号格式不正确")
    private String telephone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String location;
    
    public static UserVO fromPO(UserPO userPO) {
        UserVO userVO = new UserVO();
        userVO.setId(userPO.getId());
        userVO.setUsername(userPO.getUsername());
        userVO.setName(userPO.getName());
        userVO.setAvatar(userPO.getAvatar());
        userVO.setRole(userPO.getRole());
        userVO.setTelephone(userPO.getTelephone());
        userVO.setEmail(userPO.getEmail());
        userVO.setLocation(userPO.getLocation());
        return userVO;
    }
} 