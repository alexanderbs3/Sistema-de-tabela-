// Score.java - CORRIGIDO
package com.leaderboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "scores", indexes = {
        @Index(name = "idx_user_game", columnList = "user_id, game_id"),
        @Index(name = "idx_game_score", columnList = "game_id, score")
})
@EntityListeners(AuditingEntityListener.class)
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Min(0)
    @Column(nullable = false)
    private Integer score;

    @CreatedDate
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // ✅ CONSTRUTORES CORRIGIDOS

    // Construtor padrão (obrigatório para JPA)
    public Score() {}

    // ✅ CONSTRUTOR COM 3 PARÂMETROS (o que o ScoreService está tentando usar)
    public Score(User user, Game game, Integer score) {
        this.user = user;
        this.game = game;
        this.score = score;
        // submittedAt será definido automaticamente pelo @CreatedDate
    }

    // Construtor completo (opcional, para testes)
    public Score(Long id, User user, Game game, Integer score, LocalDateTime submittedAt) {
        this.id = id;
        this.user = user;
        this.game = game;
        this.score = score;
        this.submittedAt = submittedAt;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Score score = (Score) obj;
        return id != null && id.equals(score.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", score=" + score +
                ", submittedAt=" + submittedAt +
                '}';
    }
}