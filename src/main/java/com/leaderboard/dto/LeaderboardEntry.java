package com.leaderboard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class LeaderboardEntry {
    private Long rank;
    private String username;
    private Integer score;
    private String gameName;
    private LocalDateTime submittedAt;
}
