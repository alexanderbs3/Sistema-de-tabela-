package com.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ScoreDto {
    private Long id;
    private String username;
    private String gameName;
    private Integer score;
    private LocalDateTime submittedAt;
}
