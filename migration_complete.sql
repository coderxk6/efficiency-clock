-- =====================================================
-- 完整数据库迁移脚本
-- 用途：为 Efficiency Clock 添加多用户支持
-- =====================================================

USE antigravity;

-- 检查并添加 user_id 字段到 focus_task 表
SET @query = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE focus_task ADD COLUMN user_id BIGINT COMMENT "用户ID";',
        'SELECT "focus_task.user_id already exists" AS message;'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'antigravity'
    AND TABLE_NAME = 'focus_task'
    AND COLUMN_NAME = 'user_id'
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 user_id 字段到 user_level 表
SET @query = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE user_level ADD COLUMN user_id BIGINT UNIQUE COMMENT "用户ID";',
        'SELECT "user_level.user_id already exists" AS message;'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'antigravity'
    AND TABLE_NAME = 'user_level'
    AND COLUMN_NAME = 'user_id'
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示当前表结构
SELECT '=== focus_task 表结构 ===' AS info;
DESCRIBE focus_task;

SELECT '=== user_level 表结构 ===' AS info;
DESCRIBE user_level;

SELECT '=== user 表结构 ===' AS info;
DESCRIBE user;

SELECT '✅ 迁移完成！' AS status;
