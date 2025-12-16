-- 验证密码是否正确
-- 检查数据库中存储的MD5值是否与预期匹配

-- 验证 admin 的密码（应该是 MD5('123456') = e10adc3949ba59abbe56e057f20f883e）
SELECT 
    username,
    password,
    CASE 
        WHEN password = MD5('123456') THEN '✓ 正确'
        ELSE '✗ 错误'
    END as admin_status
FROM user 
WHERE username = 'admin';

-- 验证 user1 的密码（应该是 MD5('user123') = 6ad14ba9986e3615423dfca256d04e3f）
SELECT 
    username,
    password,
    CASE 
        WHEN password = MD5('user123') THEN '✓ 正确'
        ELSE '✗ 错误'
    END as user1_status
FROM user 
WHERE username = 'user1';

-- 验证 test 的密码（应该是 MD5('test123') = cc03e747a6afbbcbf8be7668acfebee5）
SELECT 
    username,
    password,
    CASE 
        WHEN password = MD5('test123') THEN '✓ 正确'
        ELSE '✗ 错误'
    END as test_status
FROM user 
WHERE username = 'test';

-- 如果密码不正确，执行以下更新（强制更新）
-- UPDATE user SET password = MD5('user123') WHERE username = 'user1' AND password != MD5('user123');
-- UPDATE user SET password = MD5('123456') WHERE username = 'admin' AND password != MD5('123456');
-- UPDATE user SET password = MD5('test123') WHERE username = 'test' AND password != MD5('test123');











