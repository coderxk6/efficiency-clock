INSERT INTO user_level (id, total_experience, cultivation_rank) 
SELECT 1, 0, '炼气期 - 1层'
WHERE NOT EXISTS (SELECT 1 FROM user_level WHERE id = 1);
