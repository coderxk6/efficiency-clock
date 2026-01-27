SET NAMES utf8mb4;
ALTER DATABASE antigravity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS focus_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255),
    duration_seconds INT,
    status VARCHAR(20) DEFAULT 'RUNNING',
    started_at TIMESTAMP NULL,
    expected_end_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_experience BIGINT DEFAULT 0,
    cultivation_rank VARCHAR(255) DEFAULT 'Mortal'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 强制确保现有表的编码正确
ALTER TABLE focus_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_level CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
