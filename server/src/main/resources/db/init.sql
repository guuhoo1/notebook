-- =============================================
-- 个人记事本数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 创建日期: 2026-05-03
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `notebook` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `notebook`;

-- =============================================
-- 1. 用户表 (user)
-- 存储用户账号信息和基本资料
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
  `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `gender` TINYINT DEFAULT NULL COMMENT '性别(0未知,1男,2女)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0禁用,1正常)',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0正常,1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 分类表 (category)
-- 存储笔记分类信息
-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
  `color` VARCHAR(20) DEFAULT '#181d26' COMMENT '分类颜色',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `note_count` INT NOT NULL DEFAULT 0 COMMENT '笔记数量(冗余)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0正常,1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_category_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- =============================================
-- 3. 笔记表 (note)
-- 存储笔记内容和元数据信息
-- =============================================
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `title` VARCHAR(200) NOT NULL COMMENT '笔记标题',
  `content` MEDIUMTEXT COMMENT '笔记内容(富文本HTML)',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '笔记摘要(纯文本)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0草稿,1正常,2归档)',
  `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶(0否,1是)',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0正常,1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_pinned` (`is_pinned`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  FULLTEXT KEY `ft_title_content` (`title`, `summary`),
  CONSTRAINT `fk_note_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_note_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- =============================================
-- 4. 用户设置表 (user_settings)
-- 存储用户偏好设置信息
-- =============================================
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `theme` VARCHAR(20) NOT NULL DEFAULT 'light' COMMENT '主题(light/dark)',
  `font_size` INT NOT NULL DEFAULT 14 COMMENT '字体大小',
  `font_family` VARCHAR(50) NOT NULL DEFAULT 'default' COMMENT '字体类型',
  `editor_mode` TINYINT NOT NULL DEFAULT 0 COMMENT '编辑器模式(0普通,1沉浸)',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_settings_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- =============================================
-- 5. 标签表 (tag)
-- 存储标签信息，用于笔记标记和分类
-- =============================================
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(30) NOT NULL COMMENT '标签名称',
  `color` VARCHAR(20) DEFAULT '#1b61c9' COMMENT '标签颜色',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0正常,1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_tag_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- =============================================
-- 6. 笔记标签关联表 (note_tag)
-- 存储笔记与标签的多对多关联关系
-- =============================================
DROP TABLE IF EXISTS `note_tag`;
CREATE TABLE `note_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_tag_id` (`tag_id`),
  UNIQUE KEY `uk_note_tag` (`note_id`, `tag_id`),
  CONSTRAINT `fk_note_tag_note` FOREIGN KEY (`note_id`) REFERENCES `note` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_note_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签关联表';

-- =============================================
-- 初始化测试数据
-- =============================================

-- 插入测试用户 (密码: password123，使用BCrypt加密)
INSERT INTO `user` (`email`, `phone`, `nickname`, `password`, `gender`, `status`) VALUES
('test@example.com', '13800138000', '测试用户', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', 1, 1);

-- 插入默认分类
INSERT INTO `category` (`user_id`, `name`, `description`, `color`, `icon`, `sort_order`) VALUES
(1, '工作', '工作相关笔记', '#aa2d00', 'briefcase', 0),
(1, '生活', '日常生活记录', '#0a2e0e', 'home', 1),
(1, '学习', '学习笔记和知识整理', '#1b61c9', 'book', 2);

-- 插入测试笔记
INSERT INTO `note` (`user_id`, `category_id`, `title`, `content`, `summary`, `status`, `is_pinned`) VALUES
(1, 1, '欢迎使用个人记事本', '<p>这是一个功能强大的个人记事本应用。</p><p>您可以在这里记录：</p><ul><li>工作笔记</li><li>生活点滴</li><li>学习心得</li></ul>', '这是一个功能强大的个人记事本应用。', 1, 1),
(1, 2, '今日待办事项', '<p><strong>今日待办：</strong></p><ol><li>完成项目文档</li><li>代码审查</li><li>团队会议</li></ol>', '今日待办：完成项目文档、代码审查、团队会议', 1, 0),
(1, 3, 'Vue3学习笔记', '<p>Vue3 新特性：</p><ul><li>Composition API</li><li>Teleport</li><li>Fragments</li><li>Suspense</li></ul>', 'Vue3 新特性学习笔记', 1, 0);

-- 插入用户默认设置
INSERT INTO `user_settings` (`user_id`, `theme`, `font_size`, `font_family`, `editor_mode`) VALUES
(1, 'light', 14, 'default', 0);

-- 插入测试标签
INSERT INTO `tag` (`user_id`, `name`, `color`) VALUES
(1, '重要', '#aa2d00'),
(1, '待办', '#1b61c9'),
(1, '已完成', '#006400');

-- 插入笔记标签关联
INSERT INTO `note_tag` (`note_id`, `tag_id`) VALUES
(1, 1),
(2, 2),
(3, 1),
(3, 3);

-- =============================================
-- 创建视图（可选）
-- =============================================

-- 笔记列表视图（包含分类信息）
DROP VIEW IF EXISTS `v_note_list`;
CREATE VIEW `v_note_list` AS
SELECT 
  n.id,
  n.user_id,
  n.title,
  n.summary,
  n.status,
  n.is_pinned,
  n.view_count,
  n.create_time,
  n.update_time,
  c.id AS category_id,
  c.name AS category_name,
  c.color AS category_color
FROM `note` n
LEFT JOIN `category` c ON n.category_id = c.id
WHERE n.deleted = 0;

-- =============================================
-- 创建存储过程（可选）
-- =============================================

DELIMITER //

-- 更新分类笔记数量
DROP PROCEDURE IF EXISTS `sp_update_category_note_count`//
CREATE PROCEDURE `sp_update_category_note_count`(IN p_category_id BIGINT)
BEGIN
  UPDATE `category` 
  SET `note_count` = (
    SELECT COUNT(*) FROM `note` 
    WHERE `category_id` = p_category_id AND `deleted` = 0 AND `status` = 1
  )
  WHERE `id` = p_category_id;
END//

DELIMITER ;

-- =============================================
-- 完成提示
-- =============================================
SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS user_count FROM `user`;
SELECT COUNT(*) AS category_count FROM `category`;
SELECT COUNT(*) AS note_count FROM `note`;
