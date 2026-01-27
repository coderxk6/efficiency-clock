package com.antigravity.controller;

import com.antigravity.dto.*;
import com.antigravity.entity.FocusTask;
import com.antigravity.entity.UserLevel;
import com.antigravity.mapper.FocusMapper;
import com.antigravity.mapper.UserLevelMapper;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/focus")
@CrossOrigin(origins = "*")
public class FocusController {

  private final FocusMapper focusMapper;
  private final UserLevelMapper userLevelMapper;
  private final Random random = new Random();

  private static final String[] RANKS = {
      "炼气期", "筑基期", "金丹期",
      "元婴期", "化神期", "炼虚期",
      "合体期", "大乘期", "渡劫期",
      "人仙境", "真仙境", "玄仙境",
      "金仙境", "太乙境", "大罗境",
      "道祖境", "混元无极", "创世神"
  };

  public FocusController(FocusMapper focusMapper, UserLevelMapper userLevelMapper) {
    this.focusMapper = focusMapper;
    this.userLevelMapper = userLevelMapper;
  }

  /**
   * Start a new focus task
   */
  @PostMapping("/start")
  public StartFocusResponse startFocus(@RequestBody StartFocusRequest request) {
    FocusTask task = new FocusTask();
    task.setTaskName(request.taskName());
    task.setDurationSeconds(request.durationSeconds());
    task.setStatus("RUNNING");

    LocalDateTime startedAt;
    if (request.startTime() != null && !request.startTime().isEmpty()) {
      try {
        // Parse ISO format "yyyy-MM-ddTHH:mm:ss" usually, expecting standard format
        // from frontend
        startedAt = LocalDateTime.parse(request.startTime());
      } catch (Exception e) {
        // Fallback or specific custom format
        startedAt = LocalDateTime.now();
      }
    } else {
      startedAt = LocalDateTime.now();
    }

    task.setStartedAt(startedAt);
    task.setExpectedEndAt(startedAt.plusSeconds(request.durationSeconds()));

    focusMapper.insertTask(task);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return new StartFocusResponse(
        task.getId(),
        "修炼任务已开始：" + request.taskName(),
        task.getExpectedEndAt().format(formatter));
  }

  /**
   * Complete a focus task by ID
   */
  @PutMapping("/{taskId}/complete")
  public FocusResponse completeTask(@PathVariable Long taskId) {
    FocusTask task = focusMapper.selectTaskById(taskId);

    if (task == null) {
      throw new RuntimeException("Task not found");
    }

    if ("COMPLETED".equals(task.getStatus())) {
      UserLevel user = userLevelMapper.getUserLevel();
      return new FocusResponse("修炼此前已圆满完成", user.getCultivationRank(), user.getTotalExperience(), false);
    }

    if (!"RUNNING".equals(task.getStatus())) {
      throw new RuntimeException("任务状态异常，无法完成。当前状态: " + task.getStatus());
    }

    // Update task status
    LocalDateTime now = LocalDateTime.now();
    focusMapper.updateTaskStatus(taskId, "COMPLETED", now);

    // Update User Level
    UserLevel user = userLevelMapper.getUserLevel();
    if (user == null) {
      user = new UserLevel();
      user.setId(1L);
      user.setTotalExperience(0L);
      user.setCultivationRank("炼气期 - 1层");
    }

    long expGain = task.getDurationSeconds();
    user.setTotalExperience(user.getTotalExperience() + expGain);

    // Random Rank Up Logic
    boolean levelUp = false;
    // Higher chance for rank up (33%) if task > 5 mins
    if (task.getDurationSeconds() >= 10 && random.nextInt(100) < 33) {
      int rankIndex = random.nextInt(RANKS.length);
      String newRankBase = RANKS[rankIndex];
      int stage = random.nextInt(9) + 1;
      user.setCultivationRank(newRankBase + " - " + stage + "层");
      levelUp = true;
    }

    userLevelMapper.updateUserLevel(user);

    String message = levelUp
        ? "✨ 天地异象！渡劫成功！境界提升至 " + user.getCultivationRank() + "！ ✨"
        : "修炼结束，吸收了 " + expGain + " 点天地灵气。";

    return new FocusResponse(message, user.getCultivationRank(), user.getTotalExperience(), levelUp);
  }

  /**
   * Abandon a focus task by ID
   */
  @DeleteMapping("/{taskId}")
  public void abandonTask(@PathVariable Long taskId) {
    FocusTask task = focusMapper.selectTaskById(taskId);
    if (task != null && "RUNNING".equals(task.getStatus())) {
      focusMapper.updateTaskStatus(taskId, "ABANDONED", LocalDateTime.now());
    }
  }

  /**
   * Get all running tasks
   */
  @GetMapping("/tasks")
  public List<TaskListItem> getRunningTasks() {
    List<FocusTask> tasks = focusMapper.selectRunningTasks();
    LocalDateTime now = LocalDateTime.now();

    return tasks.stream()
        .map(task -> {
          long remainingSeconds = 0;
          if (task.getExpectedEndAt() != null) {
            Duration duration = Duration.between(now, task.getExpectedEndAt());
            remainingSeconds = Math.max(0, duration.getSeconds());
          }

          return new TaskListItem(
              task.getId(),
              task.getTaskName(),
              task.getDurationSeconds(),
              task.getStatus(),
              task.getStartedAt(),
              task.getExpectedEndAt(),
              task.getCompletedAt(),
              remainingSeconds);
        })
        .collect(Collectors.toList());
  }

  /**
   * Get completed task history
   */
  @GetMapping("/history")
  public List<TaskListItem> getTaskHistory(@RequestParam(defaultValue = "50") int limit) {
    List<FocusTask> tasks = focusMapper.selectCompletedTasks(limit);

    return tasks.stream()
        .map(task -> new TaskListItem(
            task.getId(),
            task.getTaskName(),
            task.getDurationSeconds(),
            task.getStatus(),
            task.getStartedAt(),
            task.getExpectedEndAt(),
            task.getCompletedAt(),
            0L))
        .collect(Collectors.toList());
  }

  /**
   * Legacy endpoint for backward compatibility
   */
  @PostMapping("/complete")
  public FocusResponse completeFocus(@RequestBody FocusRequest request) {
    // Create and immediately complete a task (legacy behavior)
    FocusTask task = new FocusTask();
    task.setTaskName(request.taskName());
    task.setDurationSeconds(request.durationSeconds());
    task.setStatus("COMPLETED");
    task.setCompletedAt(LocalDateTime.now());
    focusMapper.insertTask(task);

    // Update User Level (same logic as before)
    UserLevel user = userLevelMapper.getUserLevel();
    if (user == null) {
      user = new UserLevel();
      user.setId(1L);
      user.setTotalExperience(0L);
      user.setCultivationRank("炼气期 - 1层");
    }

    long expGain = request.durationSeconds();
    user.setTotalExperience(user.getTotalExperience() + expGain);

    boolean levelUp = false;
    if (request.durationSeconds() >= 10 && random.nextInt(100) < 30) {
      int rankIndex = random.nextInt(RANKS.length);
      String newRankBase = RANKS[rankIndex];
      int stage = random.nextInt(9) + 1;
      user.setCultivationRank(newRankBase + " - " + stage + "层");
      levelUp = true;
    }

    userLevelMapper.updateUserLevel(user);

    String message = levelUp
        ? "渡劫成功！境界提升至 " + user.getCultivationRank() + "！"
        : "修炼结束，吸收了 " + expGain + " 点天地灵气。";

    return new FocusResponse(message, user.getCultivationRank(), user.getTotalExperience(), levelUp);
  }
}
