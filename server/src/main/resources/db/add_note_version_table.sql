-- =============================================
-- 创建笔记版本历史表
-- 用于记录笔记的每次修改，支持版本回溯
-- 日期: 2026-05-15
-- =============================================

USE `notebook`;

CREATE TABLE IF NOT EXISTS `note_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `version_number` INT NOT NULL COMMENT '版本号',
  `title` VARCHAR(200) COMMENT '版本标题',
  `content` MEDIUMTEXT COMMENT '笔记内容',
  `md_content` MEDIUMTEXT COMMENT 'Markdown内容',
  `html_content` MEDIUMTEXT COMMENT 'HTML内容',
  `summary` VARCHAR(500) COMMENT '摘要',
  `change_summary` VARCHAR(200) COMMENT '变更摘要',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_note_version_note` FOREIGN KEY (`note_id`) REFERENCES `note` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_note_version_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记版本历史表';
