-- 更新用户密码为 MD5 哈希值
-- 使用方法：在 Navicat 中执行此 SQL 脚本

-- 更新 admin 用户的密码（原始密码：123456）
UPDATE user SET password = MD5('123456') WHERE username = 'admin';

-- 更新 user1 用户的密码（原始密码：user123）
UPDATE user SET password = MD5('user123') WHERE username = 'user1';

-- 更新 test 用户的密码（原始密码：test123）
UPDATE user SET password = MD5('test123') WHERE username = 'test';

-- 验证更新结果（可选）
SELECT username, password, LENGTH(password) as pwd_length FROM user;











