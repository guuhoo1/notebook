-- 为笔记表添加分享功能相关字段
ALTER TABLE `note`
ADD COLUMN `is_public` INT(11) DEFAULT 0 COMMENT '是否公开分享（0否，1是）',
ADD COLUMN `share_code` VARCHAR(64) NULL COMMENT '分享访问码',
ADD COLUMN `share_expire_time` DATETIME NULL COMMENT '分享过期时间',
ADD COLUMN `share_view_count` INT(11) DEFAULT 0 COMMENT '分享浏览次数';

-- 添加分享码索引（用于快速查找分享笔记）
CREATE INDEX `idx_note_share_code` ON `note`(`share_code`);

-- 添加公开状态索引（用于优化查询公开笔记）
CREATE INDEX `idx_note_is_public` ON `note`(`is_public`);
