-- =============================================
-- 给 Note 表添加 deleted_at 字段，用于回收站功能
-- 日期: 2026-05-15
-- =============================================

USE `notebook`;

-- 给 note 表添加 deleted_at 字段，记录删除时间
ALTER TABLE `note` 
ADD COLUMN `deleted_at` DATETIME NULL DEFAULT NULL 
COMMENT '删除时间' 
AFTER `update_time`;
