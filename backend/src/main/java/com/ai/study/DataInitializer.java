// DataInitializer.java
package com.ai.study;

import com.ai.study.domain.User;
import com.ai.study.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        try {
            // 检查 admin 用户是否存在
            User admin = userService.findByUsername("admin");
            if (admin == null) {
                log.info("创建测试用户: admin/123456");

                User user = new User();
                user.setUsername("admin");
                user.setPassword("123456"); // 会在 UserServiceImpl 中被 MD5 加密
                user.setNickname("管理员");
                user.setRole("admin");
                user.setCreateTime(LocalDateTime.now());

                userService.register(user);
                log.info("测试用户创建成功");
            } else {
                log.info("测试用户已存在: {}", admin.getUsername());
            }
        } catch (Exception e) {
            log.error("初始化数据失败", e);
        }
    }
}