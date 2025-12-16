// AuthController.java
package com.ai.study.controller;

import com.ai.study.common.Result;
import com.ai.study.domain.User;
import com.ai.study.dto.LoginRequest;
import com.ai.study.dto.LoginResponse;
import com.ai.study.dto.RegisterRequest;
import com.ai.study.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 对输入的密码进行MD5加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                loginRequest.getPassword().getBytes(StandardCharsets.UTF_8)
        );

        // 调试信息：打印密码比较过程（生产环境可移除）
        System.out.println("登录尝试 - 用户名: " + loginRequest.getUsername());
        System.out.println("数据库中的密码: " + user.getPassword());
        System.out.println("输入密码的MD5: " + encryptedPassword);
        System.out.println("密码匹配: " + user.getPassword().equals(encryptedPassword));

        if (!user.getPassword().equals(encryptedPassword)) {
            return Result.error("用户名或密码错误");
        }

        // 创建响应数据
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        // 这里简化处理，实际应该生成JWT token
        response.setToken("jwt-token-" + user.getId() + "-" + System.currentTimeMillis());

        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        User existingUser = userService.findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setNickname(registerRequest.getNickname());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setRole("user"); // 默认角色为普通用户

        // 注册用户（密码会在service中加密）
        userService.register(newUser);

        return Result.success(null);
    }
}



