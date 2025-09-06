// GameDto.java - CORRIGIDO
package com.leaderboard.dto;

import java.time.LocalDateTime;

public class GameDto {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Integer totalPlayers;
    private Integer totalScores;

    // ✅ CONSTRUTORES CORRIGIDOS

    // Construtor padrão
    public GameDto() {}

    // ✅ CONSTRUTOR COM 4 PARÂMETROS (o que o GameService está tentando usar)
    public GameDto(Long id, String name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        // totalPlayers e totalScores serão definidos depois, se necessário
    }

    // Construtor completo com 6 parâmetros
    public GameDto(Long id, String name, String description, LocalDateTime createdAt,
                   Integer totalPlayers, Integer totalScores) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.totalPlayers = totalPlayers;
        this.totalScores = totalScores;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getTotalPlayers() { return totalPlayers; }
    public void setTotalPlayers(Integer totalPlayers) { this.totalPlayers = totalPlayers; }

    public Integer getTotalScores() { return totalScores; }
    public void setTotalScores(Integer totalScores) { this.totalScores = totalScores; }
}