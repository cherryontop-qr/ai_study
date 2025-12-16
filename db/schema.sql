-- 智能学习管理系统数据库建表脚本
-- 数据库名：aistudy
-- 字符集：utf8mb4

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（MD5加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 学习任务表
CREATE TABLE IF NOT EXISTS `study_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
  `description` TEXT COMMENT '任务描述',
  `target_hours` INT NOT NULL COMMENT '目标学习时长（小时）',
  `status` VARCHAR(20) DEFAULT 'TODO' COMMENT '状态：TODO/DOING/DONE',
  `category` VARCHAR(100) DEFAULT NULL COMMENT '学习分类',
  `deadline` DATE DEFAULT NULL COMMENT '截止日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`),
  CONSTRAINT `fk_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务表';

-- 3. 学习记录表（打卡记录）
CREATE TABLE IF NOT EXISTS `study_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `study_date` DATE NOT NULL COMMENT '学习日期',
  `duration_minutes` INT NOT NULL COMMENT '学习时长（分钟）',
  `comment` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_study_date` (`study_date`),
  CONSTRAINT `fk_record_task` FOREIGN KEY (`task_id`) REFERENCES `study_task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 插入测试数据（可选）
-- 默认管理员账号：admin / 123456
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN')
ON DUPLICATE KEY UPDATE `username`=`username`;


