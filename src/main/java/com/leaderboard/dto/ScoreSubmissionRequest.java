package com.leaderboard.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ScoreSubmissionRequest {

    @NotNull(message = "Game ID é obrigatorio")
    private Long gameId;

    @NotNull(message = "Score é obrigatorio")
    @Min(value = 0,message = "Score debe ser maior ou igual a 0")
    private Integer score;
}
