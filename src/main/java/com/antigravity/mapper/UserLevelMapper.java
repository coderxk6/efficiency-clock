package com.antigravity.mapper;

import com.antigravity.entity.UserLevel;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserLevelMapper {

  @Select("SELECT * FROM user_level WHERE user_id = #{userId}")
  Optional<UserLevel> getUserLevelByUserId(Long userId);

  @Insert("INSERT INTO user_level (user_id, total_experience, cultivation_rank) " +
          "VALUES (#{userId}, #{totalExperience}, #{cultivationRank})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertUserLevel(UserLevel userLevel);

  @Update("UPDATE user_level SET total_experience = #{totalExperience}, " +
          "cultivation_rank = #{cultivationRank} WHERE user_id = #{userId}")
  void updateUserLevel(UserLevel userLevel);

  // 保留旧的方法用于兼容性
  @Select("SELECT * FROM user_level WHERE id = 1")
  UserLevel getUserLevel();
}
