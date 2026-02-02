-- 数据库迁移脚本：添加用户支持
-- 执行此脚本来更新数据库结构以支持多用户

USE antigravity;

-- 1. 为 focus_task 表添加 user_id 字段
ALTER TABLE focus_task 
ADD COLUMN user_id BIGINT COMMENT '用户ID';

-- 2. 为 user_level 表添加 user_id 字段
ALTER TABLE user_level 
ADD COLUMN user_id BIGINT UNIQUE COMMENT '用户ID';

-- 3. 添加外键约束（如果 user 表已存在）
-- 注意：如果 user 表还不存在，这些语句会失败，可以稍后再执行
ALTER TABLE focus_task 
ADD CONSTRAINT fk_focus_task_user 
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

ALTER TABLE user_level 
ADD CONSTRAINT fk_user_level_user 
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- 4. 为现有数据设置默认 user_id（可选）
-- 如果你有现有的数据，可以创建一个默认用户并关联
-- 取消下面的注释来执行：

/*
-- 创建一个默认用户
INSERT INTO user (username, password, nickname, is_guest, created_at, updated_at)
VALUES ('default_user', '$2a$10$XYZ...', '默认用户', FALSE, NOW(), NOW());

-- 获取刚创建的用户 ID 并更新现有数据
SET @default_user_id = LAST_INSERT_ID();

UPDATE focus_task SET user_id = @default_user_id WHERE user_id IS NULL;
UPDATE user_level SET user_id = @default_user_id WHERE user_id IS NULL;
*/

SELECT 'Migration completed successfully!' AS status;
