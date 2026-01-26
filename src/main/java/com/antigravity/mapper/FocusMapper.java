package com.antigravity.mapper;

import com.antigravity.entity.FocusTask;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FocusMapper {

  @Insert("INSERT INTO focus_task (task_name, duration_seconds, status, started_at, expected_end_at, completed_at) " +
      "VALUES (#{taskName}, #{durationSeconds}, #{status}, #{startedAt}, #{expectedEndAt}, #{completedAt})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertTask(FocusTask task);

  @Select("SELECT * FROM focus_task WHERE status = 'RUNNING' ORDER BY started_at DESC")
  List<FocusTask> selectRunningTasks();

  @Select("SELECT * FROM focus_task WHERE status = 'COMPLETED' ORDER BY completed_at DESC LIMIT #{limit}")
  List<FocusTask> selectCompletedTasks(int limit);

  @Update("UPDATE focus_task SET status = #{status}, completed_at = #{completedAt} WHERE id = #{id}")
  void updateTaskStatus(@Param("id") Long id, @Param("status") String status,
      @Param("completedAt") LocalDateTime completedAt);

  @Select("SELECT * FROM focus_task WHERE id = #{id}")
  FocusTask selectTaskById(Long id);
}
