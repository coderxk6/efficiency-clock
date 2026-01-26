package com.antigravity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FocusTask {
  private Long id;
  private String taskName;
  private Integer durationSeconds;
  private String status;
  private LocalDateTime startedAt;
  private LocalDateTime expectedEndAt;
  private LocalDateTime completedAt;
}
