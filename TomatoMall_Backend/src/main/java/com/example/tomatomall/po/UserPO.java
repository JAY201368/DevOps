package com.example.tomatomall.po;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Entity
@Setter
@Getter
@Table(name = "users")
public class UserPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Column(nullable = false)
    private String name;

    private String avatar;

    @NotBlank(message = "角色不能为空")
    private String role;

    @Pattern(regexp = "^1[0-9]{10}$", message = "手机号格式不正确")
    private String telephone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String location;
} 