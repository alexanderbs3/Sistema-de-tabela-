package com.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UserRankingDto {
    private String username;
    private Long globalRank;
    private Integer bestScore;
    private Integer totalScores;
    private Double averageScore;
}
