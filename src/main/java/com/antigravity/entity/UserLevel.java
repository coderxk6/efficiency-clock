package com.antigravity.entity;

import lombok.Data;

@Data
public class UserLevel {
  private Long id;
  private Long totalExperience;
  private String cultivationRank; // e.g. "Qi Refining", "Foundation Establishment"
}
