package com.antigravity.dto;

import java.time.LocalDateTime;

public record TaskListItem(
    Long id,
    String taskName,
    Integer durationSeconds,
    String status,
    LocalDateTime startedAt,
    LocalDateTime expectedEndAt,
    LocalDateTime completedAt,
    Long remainingSeconds) {
}
