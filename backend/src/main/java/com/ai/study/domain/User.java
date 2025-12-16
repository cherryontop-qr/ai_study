// User.java (根据你的 user 表结构)
package com.ai.study.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String role;
    private LocalDateTime createTime;
}













