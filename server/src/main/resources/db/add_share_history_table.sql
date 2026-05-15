-- =============================================
-- 添加分享浏览历史表
-- 数据库: MySQL 8.0+
-- 创建日期: 2026-05-04
-- =============================================

USE `notebook`;

-- =============================================
-- 7. 分享浏览历史表 (share_history)
-- 存储用户访问分享笔记的浏览记录
-- =============================================
DROP TABLE IF EXISTS `share_history`;
CREATE TABLE `share_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（未登录用户为NULL）',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `share_code` VARCHAR(50) NOT NULL COMMENT '分享码',
  `visit_ip` VARCHAR(50) DEFAULT NULL COMMENT '访问IP地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0正常,1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_share_code` (`share_code`),
  KEY `idx_update_time` (`update_time`),
  KEY `idx_user_share` (`user_id`, `share_code`),
  CONSTRAINT `fk_share_history_note` FOREIGN KEY (`note_id`) REFERENCES `note` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享浏览历史表';

-- =============================================
-- 完成提示
-- =============================================
SELECT '分享浏览历史表创建完成！' AS message;
