package com.leaderboard.service;

import com.leaderboard.dto.LeaderboardEntry;
import com.leaderboard.entity.Game;
import com.leaderboard.entity.Score;
import com.leaderboard.repository.GameRepository;
import com.leaderboard.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class LeaderboardService {

    @Autowired
    private RedisLeaderboardService redisLeaderboardService;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserService userService;

    /**
     * Obtém o leaderboard global do Redis, com fallback para o banco
     */
    public List<LeaderboardEntry> getGlobalLeaderboard(int limit) {
        try {
            // Primeiro, tenta buscar do Redis
            List<LeaderboardEntry> redisResults = redisLeaderboardService.getGlobalTopN(limit);

            if (redisResults != null && !redisResults.isEmpty()) {
                return redisResults;
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar leaderboard do Redis: " + e.getMessage());
        }

        // Fallback para o banco de dados
        return getGlobalLeaderboardFromDatabase(limit);
    }

    /**
     * Obtém o leaderboard de um jogo específico do Redis, com fallback para o banco
     */
    public List<LeaderboardEntry> getGameLeaderboard(Long gameId, int limit) {
        try {
            // Primeiro, tenta buscar do Redis
            List<LeaderboardEntry> redisResults = redisLeaderboardService.getGameTopN(gameId, limit);

            if (redisResults != null && !redisResults.isEmpty()) {
                return enrichWithGameName(redisResults, gameId);
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar leaderboard do jogo do Redis: " + e.getMessage());
        }

        // Fallback para o banco de dados
        return getGameLeaderboardFromDatabase(gameId, limit);
    }

    /**
     * Obtém o leaderboard global do banco de dados
     */
    private List<LeaderboardEntry> getGlobalLeaderboardFromDatabase(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Score> topScores = scoreRepository.findGlobalLeaderboard(pageable);

        List<LeaderboardEntry> entries = new ArrayList<>();
        long rank = 1;

        for (Score score : topScores.getContent()) {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setRank(rank++);
            entry.setUsername(score.getUser().getUsername());
            entry.setScore(score.getScore());
            entry.setGameName("Global");
            entry.setSubmittedAt(score.getSubmittedAt());
            entries.add(entry);
        }

        return entries;
    }

    /**
     * Obtém o leaderboard de um jogo do banco de dados
     */
    private List<LeaderboardEntry> getGameLeaderboardFromDatabase(Long gameId, int limit) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));

        Pageable pageable = PageRequest.of(0, limit);
        Page<Score> topScores = scoreRepository.findGameLeaderboard(game, pageable);

        List<LeaderboardEntry> entries = new ArrayList<>();
        long rank = 1;

        for (Score score : topScores.getContent()) {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setRank(rank++);
            entry.setUsername(score.getUser().getUsername());
            entry.setScore(score.getScore());
            entry.setGameName(game.getName());
            entry.setSubmittedAt(score.getSubmittedAt());
            entries.add(entry);
        }

        return entries;
    }

    /**
     * Obtém a posição do usuário no ranking global
     */
    public Long getUserGlobalRank(String username) {
        try {
            // Primeiro tenta do Redis
            Long redisRank = redisLeaderboardService.getUserGlobalRank(username);
            if (redisRank != null) {
                return redisRank;
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar rank do Redis: " + e.getMessage());
        }

        // Fallback para o banco
        return getUserGlobalRankFromDatabase(username);
    }

    /**
     * Obtém a posição do usuário no ranking de um jogo
     */
    public Long getUserGameRank(Long gameId, String username) {
        try {
            // Primeiro tenta do Redis
            Long redisRank = redisLeaderboardService.getUserGameRank(gameId, username);
            if (redisRank != null) {
                return redisRank;
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar rank do jogo do Redis: " + e.getMessage());
        }

        // Fallback para o banco
        return getUserGameRankFromDatabase(gameId, username);
    }

    /**
     * Busca rank global do usuário no banco de dados
     */
    private Long getUserGlobalRankFromDatabase(String username) {
        try {
            var user = userService.findByUsername(username);
            return scoreRepository.findUserGlobalRank(user.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca rank do usuário no jogo no banco de dados
     */
    private Long getUserGameRankFromDatabase(Long gameId, String username) {
        try {
            var user = userService.findByUsername(username);
            return scoreRepository.findUserRankInGame(user.getId(), gameId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtém jogadores próximos ao usuário no ranking global
     */
    public List<LeaderboardEntry> getUserNeighbors(String username, int neighbors) {
        try {
            List<LeaderboardEntry> redisResults = redisLeaderboardService.getUserNeighbors(username, neighbors);
            if (redisResults != null && !redisResults.isEmpty()) {
                return redisResults;
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar vizinhos do Redis: " + e.getMessage());
        }

        // Fallback para implementação do banco (simplificada)
        Long userRank = getUserGlobalRank(username);
        if (userRank == null) return new ArrayList<>();

        long start = Math.max(1, userRank - neighbors);
        long end = userRank + neighbors;

        return getGlobalRankRange((int) start, (int) end);
    }

    /**
     * Obtém jogadores próximos ao usuário no ranking de um jogo
     */
    public List<LeaderboardEntry> getUserGameNeighbors(Long gameId, String username, int neighbors) {
        try {
            List<LeaderboardEntry> redisResults = redisLeaderboardService.getUserGameNeighbors(gameId, username, neighbors);
            if (redisResults != null && !redisResults.isEmpty()) {
                return enrichWithGameName(redisResults, gameId);
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar vizinhos do jogo do Redis: " + e.getMessage());
        }

        // Fallback para implementação do banco
        Long userRank = getUserGameRank(gameId, username);
        if (userRank == null) return new ArrayList<>();

        long start = Math.max(1, userRank - neighbors);
        long end = userRank + neighbors;

        return getGameRankRange(gameId, (int) start, (int) end);
    }

    /**
     * Obtém uma faixa específica do ranking global
     */
    public List<LeaderboardEntry> getGlobalRankRange(int start, int end) {
        int limit = end - start + 1;
        List<LeaderboardEntry> fullList = getGlobalLeaderboard(Math.max(end, 100));

        if (start > fullList.size()) return new ArrayList<>();

        int endIndex = Math.min(end, fullList.size());
        return fullList.subList(start - 1, endIndex);
    }

    /**
     * Obtém uma faixa específica do ranking de um jogo
     */
    public List<LeaderboardEntry> getGameRankRange(Long gameId, int start, int end) {
        int limit = end - start + 1;
        List<LeaderboardEntry> fullList = getGameLeaderboard(gameId, Math.max(end, 100));

        if (start > fullList.size()) return new ArrayList<>();

        int endIndex = Math.min(end, fullList.size());
        return fullList.subList(start - 1, endIndex);
    }

    /**
     * Sincroniza os leaderboards do Redis com os dados do banco
     */
    @Async
    public CompletableFuture<Void> syncLeaderboards() {
        try {
            System.out.println("Iniciando sincronização dos leaderboards...");

            // Limpar leaderboards existentes
            redisLeaderboardService.clearGlobalLeaderboard();

            // Sincronizar leaderboard global
            syncGlobalLeaderboard();

            // Sincronizar leaderboards por jogo
            syncGameLeaderboards();

            System.out.println("Sincronização dos leaderboards concluída!");

        } catch (Exception e) {
            System.err.println("Erro na sincronização dos leaderboards: " + e.getMessage());
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sincroniza o leaderboard global
     */
    private void syncGlobalLeaderboard() {
        Pageable pageable = PageRequest.of(0, 1000); // Sincronizar top 1000
        Page<Score> topScores = scoreRepository.findGlobalLeaderboard(pageable);

        for (Score score : topScores.getContent()) {
            redisLeaderboardService.updateGlobalLeaderboard(
                    score.getUser().getUsername(),
                    score.getScore()
            );
            redisLeaderboardService.updateUserBestScore(
                    score.getUser().getUsername(),
                    score.getScore()
            );
        }

        System.out.println("Leaderboard global sincronizado: " + topScores.getNumberOfElements() + " entries");
    }

    /**
     * Sincroniza os leaderboards por jogo
     */
    private void syncGameLeaderboards() {
        List<Game> games = gameRepository.findAll();

        for (Game game : games) {
            Pageable pageable = PageRequest.of(0, 500); // Top 500 por jogo
            Page<Score> gameScores = scoreRepository.findGameLeaderboard(game, pageable);

            for (Score score : gameScores.getContent()) {
                redisLeaderboardService.updateGameLeaderboard(
                        game.getId(),
                        score.getUser().getUsername(),
                        score.getScore()
                );
            }

            System.out.println("Leaderboard do jogo " + game.getName() + " sincronizado: " +
                    gameScores.getNumberOfElements() + " entries");
        }
    }

    /**
     * Obtém estatísticas dos leaderboards
     */
    public LeaderboardStats getLeaderboardStatistics() {
        LeaderboardStats stats = new LeaderboardStats();

        try {
            // Estatísticas do Redis
            RedisLeaderboardService.LeaderboardStats redisStats = redisLeaderboardService.getLeaderboardStats();
            stats.setGlobalPlayersInRedis(redisStats.getGlobalPlayersCount());
            stats.setRedisHealthy(redisStats.isRedisHealthy());

            // Estatísticas do banco
            stats.setTotalPlayersInDb(scoreRepository.countDistinctPlayers());
            stats.setTotalScoresInDb(scoreRepository.count());
            stats.setTotalGames(gameRepository.count());

            // Verificar sincronização
            stats.setSyncNeeded(stats.getGlobalPlayersInRedis() == null ||
                    stats.getGlobalPlayersInRedis() < stats.getTotalPlayersInDb() / 2);

        } catch (Exception e) {
            stats.setRedisHealthy(false);
            stats.setSyncNeeded(true);
        }

        return stats;
    }

    /**
     * Obtém top performers em um período específico
     */
    public List<LeaderboardEntry> getTopPerformersInPeriod(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Object[]> results = scoreRepository.findTopPerformersInPeriod(startDate, endDate, pageable);

        List<LeaderboardEntry> entries = new ArrayList<>();
        long rank = 1;

        for (Object[] result : results.getContent()) {
            var user = (com.leaderboard.entity.User) result[0];
            Integer bestScore = result[1] != null ? ((Number) result[1]).intValue() : 0;
            Integer totalScores = result[2] != null ? ((Number) result[2]).intValue() : 0;
            Double avgScore = result[3] != null ? ((Number) result[3]).doubleValue() : 0.0;

            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setRank(rank++);
            entry.setUsername(user.getUsername());
            entry.setScore(bestScore);
            entry.setGameName("Período: " + startDate.toLocalDate() + " - " + endDate.toLocalDate());

            entries.add(entry);
        }

        return entries;
    }

    /**
     * Enriquece as entradas do leaderboard com o nome do jogo
     */
    private List<LeaderboardEntry> enrichWithGameName(List<LeaderboardEntry> entries, Long gameId) {
        try {
            Game game = gameRepository.findById(gameId).orElse(null);
            String gameName = game != null ? game.getName() : "Game " + gameId;

            entries.forEach(entry -> entry.setGameName(gameName));
        } catch (Exception e) {
            // Se não conseguir buscar o nome do jogo, deixa como está
        }

        return entries;
    }

    /**
     * Força a atualização do leaderboard para um usuário específico
     */
    public void forceUpdateUserInLeaderboards(String username) {
        try {
            var user = userService.findByUsername(username);

            // Buscar melhor score global do usuário
            Object[] globalStats = scoreRepository.findUserStatistics(user);
            if (globalStats[0] != null) {
                Integer bestScore = ((Number) globalStats[0]).intValue();
                redisLeaderboardService.updateGlobalLeaderboard(username, bestScore);
                redisLeaderboardService.updateUserBestScore(username, bestScore);
            }

            // Atualizar por jogo
            List<Game> games = gameRepository.findAll();
            for (Game game : games) {
                Object[] gameStats = scoreRepository.findUserStatisticsForGame(user, game);
                if (gameStats[0] != null) {
                    Integer bestScore = ((Number) gameStats[0]).intValue();
                    redisLeaderboardService.updateGameLeaderboard(game.getId(), username, bestScore);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao forçar atualização do usuário " + username + ": " + e.getMessage());
        }
    }

    /**
     * Verifica se o Redis está funcionando corretamente
     */
    public boolean isRedisHealthy() {
        return redisLeaderboardService.isLeaderboardHealthy();
    }

    // Classe para estatísticas do leaderboard
    public static class LeaderboardStats {
        private Long globalPlayersInRedis;
        private Long totalPlayersInDb;
        private Long totalScoresInDb;
        private Long totalGames;
        private boolean redisHealthy;
        private boolean syncNeeded;

        // Getters e Setters
        public Long getGlobalPlayersInRedis() { return globalPlayersInRedis; }
        public void setGlobalPlayersInRedis(Long globalPlayersInRedis) { this.globalPlayersInRedis = globalPlayersInRedis; }

        public Long getTotalPlayersInDb() { return totalPlayersInDb; }
        public void setTotalPlayersInDb(Long totalPlayersInDb) { this.totalPlayersInDb = totalPlayersInDb; }

        public Long getTotalScoresInDb() { return totalScoresInDb; }
        public void setTotalScoresInDb(Long totalScoresInDb) { this.totalScoresInDb = totalScoresInDb; }

        public Long getTotalGames() { return totalGames; }
        public void setTotalGames(Long totalGames) { this.totalGames = totalGames; }

        public boolean isRedisHealthy() { return redisHealthy; }
        public void setRedisHealthy(boolean redisHealthy) { this.redisHealthy = redisHealthy; }

        public boolean isSyncNeeded() { return syncNeeded; }
        public void setSyncNeeded(boolean syncNeeded) { this.syncNeeded = syncNeeded; }
    }
}