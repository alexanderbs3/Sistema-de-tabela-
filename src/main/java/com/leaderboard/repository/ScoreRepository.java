// ScoreRepository.java - VERSÃO FINAL CORRIGIDA
package com.leaderboard.repository;

import com.leaderboard.entity.Game;
import com.leaderboard.entity.Score;
import com.leaderboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    // Buscar scores por usuário
    List<Score> findByUserOrderByScoreDesc(User user);

    List<Score> findByUserAndGameOrderByScoreDesc(User user, Game game);

    // Buscar melhor score de um usuário em um jogo
    @Query("SELECT s FROM Score s WHERE s.user = :user AND s.game = :game ORDER BY s.score DESC LIMIT 1")
    Optional<Score> findTopByUserAndGameOrderByScoreDesc(@Param("user") User user, @Param("game") Game game);

    // Buscar scores por jogo
    List<Score> findByGameOrderByScoreDesc(Game game);

    Page<Score> findByGameOrderByScoreDesc(Game game, Pageable pageable);

    // Top scores globais
    @Query("SELECT s FROM Score s ORDER BY s.score DESC")
    Page<Score> findTopScoresGlobal(Pageable pageable);

    // Top scores por jogo
    @Query("SELECT s FROM Score s WHERE s.game = :game ORDER BY s.score DESC")
    Page<Score> findTopScoresByGame(@Param("game") Game game, Pageable pageable);

    // Ranking global - melhor score por usuário
    @Query("""
        SELECT s FROM Score s 
        WHERE s.score = (SELECT MAX(s2.score) FROM Score s2 WHERE s2.user = s.user)
        ORDER BY s.score DESC
        """)
    Page<Score> findGlobalLeaderboard(Pageable pageable);

    // Ranking por jogo - melhor score por usuário
    @Query("""
        SELECT s FROM Score s 
        WHERE s.game = :game 
        AND s.score = (SELECT MAX(s2.score) FROM Score s2 WHERE s2.user = s.user AND s2.game = :game)
        ORDER BY s.score DESC
        """)
    Page<Score> findGameLeaderboard(@Param("game") Game game, Pageable pageable);

    // Estatísticas do usuário
    @Query("""
        SELECT MAX(s.score) as bestScore,
               COUNT(s) as totalScores,
               AVG(s.score) as averageScore
        FROM Score s 
        WHERE s.user = :user
        """)
    Object[] findUserStatistics(@Param("user") User user);

    @Query("""
        SELECT MAX(s.score) as bestScore,
               COUNT(s) as totalScores,
               AVG(s.score) as averageScore
        FROM Score s 
        WHERE s.user = :user AND s.game = :game
        """)
    Object[] findUserStatisticsForGame(@Param("user") User user, @Param("game") Game game);

    // Encontrar posição do usuário no ranking global
    @Query(value = """
        WITH ranked_users AS (
            SELECT DISTINCT u.id as user_id, 
                   MAX(s.score) as best_score,
                   RANK() OVER (ORDER BY MAX(s.score) DESC) as rank
            FROM scores s
            JOIN users u ON s.user_id = u.id
            GROUP BY u.id
        )
        SELECT rank FROM ranked_users WHERE user_id = :userId
        """, nativeQuery = true)
    Optional<Long> findUserGlobalRank(@Param("userId") Long userId);

    // Encontrar posição do usuário no ranking do jogo
    @Query(value = """
        WITH ranked_users AS (
            SELECT DISTINCT u.id as user_id, 
                   MAX(s.score) as best_score,
                   RANK() OVER (ORDER BY MAX(s.score) DESC) as rank
            FROM scores s
            JOIN users u ON s.user_id = u.id
            WHERE s.game_id = :gameId
            GROUP BY u.id
        )
        SELECT rank FROM ranked_users WHERE user_id = :userId
        """, nativeQuery = true)
    Optional<Long> findUserRankInGame(@Param("userId") Long userId, @Param("gameId") Long gameId);

    // Scores recentes
    List<Score> findTop10ByOrderBySubmittedAtDesc();

    List<Score> findTop10ByGameOrderBySubmittedAtDesc(Game game);

    // Scores em um período
    List<Score> findBySubmittedAtBetweenOrderByScoreDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Score s WHERE s.game = :game AND s.submittedAt BETWEEN :startDate AND :endDate ORDER BY s.score DESC")
    List<Score> findByGameAndSubmittedAtBetweenOrderByScoreDesc(
            @Param("game") Game game,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Top performers em um período
    @Query("""
        SELECT s.user, MAX(s.score) as bestScore, COUNT(s) as totalScores, AVG(s.score) as avgScore
        FROM Score s 
        WHERE s.submittedAt BETWEEN :startDate AND :endDate
        GROUP BY s.user
        ORDER BY MAX(s.score) DESC
        """)
    Page<Object[]> findTopPerformersInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // ✅ MÉTODO SEGURO: Verificar se usuário submeteu score em período específico
    @Query("""
        SELECT COUNT(s) > 0 
        FROM Score s 
        WHERE s.user = :user 
        AND s.game = :game 
        AND s.submittedAt >= :startOfDay
        AND s.submittedAt < :endOfDay
        """)
    boolean existsByUserAndGameBetweenDates(
            @Param("user") User user,
            @Param("game") Game game,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // Contar total de jogadores únicos
    @Query("SELECT COUNT(DISTINCT s.user) FROM Score s")
    Long countDistinctPlayers();

    // Contar total de jogadores únicos por jogo
    @Query("SELECT COUNT(DISTINCT s.user) FROM Score s WHERE s.game = :game")
    Long countDistinctPlayersByGame(@Param("game") Game game);
}
