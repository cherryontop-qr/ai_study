package com.ai.study.service;

import com.ai.study.domain.User;

public interface UserService {

    User findByUsername(String username);

    void register(User user);
}
















