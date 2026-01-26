package com.antigravity.mapper;

import com.antigravity.entity.UserLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserLevelMapper {

  @Select("SELECT * FROM user_level WHERE id = 1")
  UserLevel getUserLevel();

  @Update("UPDATE user_level SET total_experience = #{totalExperience}, cultivation_rank = #{cultivationRank} WHERE id = 1")
  void updateUserLevel(UserLevel userLevel);
}
