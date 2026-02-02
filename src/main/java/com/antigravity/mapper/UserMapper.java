package com.antigravity.mapper;

import com.antigravity.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    Optional<User> findById(Long id);

    @Insert("INSERT INTO user (username, password, nickname, is_guest) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{isGuest})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE user SET nickname = #{nickname}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    void update(User user);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    boolean existsByUsername(String username);
}
