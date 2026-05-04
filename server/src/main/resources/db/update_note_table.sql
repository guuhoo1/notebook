-- =============================================
-- 更新 note 表结构脚本
-- 添加 Markdown 相关字段
-- 数据库: MySQL 8.0+
-- 日期: 2026-05-04
-- =============================================

USE `notebook`;

-- 添加 md_content 字段（Markdown原始内容）
ALTER TABLE `note` ADD COLUMN `md_content` MEDIUMTEXT COMMENT 'Markdown原始内容(用于编辑)' AFTER `content`;

-- 添加 html_content 字段（编译后的HTML内容）
ALTER TABLE `note` ADD COLUMN `html_content` MEDIUMTEXT COMMENT '编译后的HTML内容(用于预览)' AFTER `md_content`;

-- 验证表结构
DESCRIBE `note`;

-- 显示更新后的 note 表结构
SELECT '表结构更新完成！' AS message;
