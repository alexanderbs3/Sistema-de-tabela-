package com.leaderboard.service;

import com.leaderboard.dto.LeaderboardEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor

@Service
public class RedisLeaderboardService {
    private static final String GLOBAL_LEADERBOARD_KEY = "leaderboard:global";
    private static final String GAME_LEADERBOARD_PREFIX = "leaderboard:game:";
    private static final String USER_BEST_SCORES_PREFIX = "user:best_scores:";

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Adiciona ou atualiza o score do usuário no leaderboard global
     */
    public void updateGlobalLeaderboard(String username, Integer score) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Verifica se o usuário já tem um score melhor
        Double currentScore = zSetOps.score(GLOBAL_LEADERBOARD_KEY, username);
        if (currentScore == null || score > currentScore.intValue()) {
            zSetOps.add(GLOBAL_LEADERBOARD_KEY, username, score);
        }
    }

    /**
     * Adiciona ou atualiza o score do usuário no leaderboard de um jogo específico
     */
    public void updateGameLeaderboard(Long gameId, String username, Integer score) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Verifica se o usuário já tem um score melhor para este jogo
        Double currentScore = zSetOps.score(gameKey, username);
        if (currentScore == null || score > currentScore.intValue()) {
            zSetOps.add(gameKey, username, score);

            // Também atualiza o leaderboard global se necessário
            updateGlobalLeaderboard(username, score);
        }
    }

    /**
     * Obtém o top N do leaderboard global
     */
    public List<LeaderboardEntry> getGlobalTopN(int limit) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Busca em ordem reversa (maior para menor score)
        Set<ZSetOperations.TypedTuple<Object>> topUsers =
                zSetOps.reverseRangeWithScores(GLOBAL_LEADERBOARD_KEY, 0, limit - 1);

        return convertToLeaderboardEntries(topUsers, "Global");
    }

    /**
     * Obtém o top N do leaderboard de um jogo específico
     */
    public List<LeaderboardEntry> getGameTopN(Long gameId, int limit) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        Set<ZSetOperations.TypedTuple<Object>> topUsers =
                zSetOps.reverseRangeWithScores(gameKey, 0, limit - 1);

        return convertToLeaderboardEntries(topUsers, "Game " + gameId);
    }

    /**
     * Obtém a posição do usuário no ranking global
     */
    public Long getUserGlobalRank(String username) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Long rank = zSetOps.reverseRank(GLOBAL_LEADERBOARD_KEY, username);
        return rank != null ? rank + 1 : null; // Redis usa índice 0, queremos começar em 1
    }

    /**
     * Obtém a posição do usuário no ranking de um jogo
     */
    public Long getUserGameRank(Long gameId, String username) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Long rank = zSetOps.reverseRank(gameKey, username);
        return rank != null ? rank + 1 : null;
    }

    /**
     * Obtém o score do usuário no leaderboard global
     */
    public Integer getUserGlobalScore(String username) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Double score = zSetOps.score(GLOBAL_LEADERBOARD_KEY, username);
        return score != null ? score.intValue() : null;
    }

    /**
     * Obtém o score do usuário em um jogo específico
     */
    public Integer getUserGameScore(Long gameId, String username) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Double score = zSetOps.score(gameKey, username);
        return score != null ? score.intValue() : null;
    }

    /**
     * Remove usuário do leaderboard global
     */
    public void removeFromGlobalLeaderboard(String username) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.remove(GLOBAL_LEADERBOARD_KEY, username);
    }

    /**
     * Remove usuário do leaderboard de um jogo
     */
    public void removeFromGameLeaderboard(Long gameId, String username) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.remove(gameKey, username);
    }

    /**
     * Obtém o total de jogadores no leaderboard global
     */
    public Long getGlobalLeaderboardSize() {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.zCard(GLOBAL_LEADERBOARD_KEY);
    }

    /**
     * Obtém o total de jogadores no leaderboard de um jogo
     */
    public Long getGameLeaderboardSize(Long gameId) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.zCard(gameKey);
    }

    /**
     * Obtém jogadores em uma faixa específica do ranking global
     */
    public List<LeaderboardEntry> getGlobalRankRange(long start, long end) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> rangeUsers =
                zSetOps.reverseRangeWithScores(GLOBAL_LEADERBOARD_KEY, start - 1, end - 1);

        return convertToLeaderboardEntries(rangeUsers, "Global", start);
    }

    /**
     * Obtém jogadores em uma faixa específica do ranking de um jogo
     */
    public List<LeaderboardEntry> getGameRankRange(Long gameId, long start, long end) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> rangeUsers =
                zSetOps.reverseRangeWithScores(gameKey, start - 1, end - 1);

        return convertToLeaderboardEntries(rangeUsers, "Game " + gameId, start);
    }

    /**
     * Limpa todo o leaderboard de um jogo
     */
    public void clearGameLeaderboard(Long gameId) {
        String gameKey = GAME_LEADERBOARD_PREFIX + gameId;
        redisTemplate.delete(gameKey);
    }

    /**
     * Limpa o leaderboard global
     */
    public void clearGlobalLeaderboard() {
        redisTemplate.delete(GLOBAL_LEADERBOARD_KEY);
    }

    /**
     * Sincroniza leaderboards do Redis com dados do banco
     */
    public void syncLeaderboards() {
        // Este método será chamado pelo serviço principal para sincronizar dados
        clearGlobalLeaderboard();
        // A lógica de sincronização será implementada no LeaderboardService
    }

    /**
     * Obtém usuários próximos de um usuário específico no ranking
     */
    public List<LeaderboardEntry> getUserNeighbors(String username, int neighbors) {
        Long rank = getUserGlobalRank(username);
        if (rank == null) return new ArrayList<>();

        long start = Math.max(1, rank - neighbors);
        long end = rank + neighbors;

        return getGlobalRankRange(start, end);
    }

    /**
     * Obtém usuários próximos de um usuário específico no ranking de um jogo
     */
    public List<LeaderboardEntry> getUserGameNeighbors(Long gameId, String username, int neighbors) {
        Long rank = getUserGameRank(gameId, username);
        if (rank == null) return new ArrayList<>();

        long start = Math.max(1, rank - neighbors);
        long end = rank + neighbors;

        return getGameRankRange(gameId, start, end);
    }

    /**
     * Converte TypedTuple do Redis para LeaderboardEntry
     */
    private List<LeaderboardEntry> convertToLeaderboardEntries(
            Set<ZSetOperations.TypedTuple<Object>> tuples, String gameName) {
        return convertToLeaderboardEntries(tuples, gameName, 1L);
    }

    /**
     * Converte TypedTuple do Redis para LeaderboardEntry com rank inicial customizado
     */
    private List<LeaderboardEntry> convertToLeaderboardEntries(
            Set<ZSetOperations.TypedTuple<Object>> tuples, String gameName, long startRank) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        long rank = startRank;

        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            String username = (String) tuple.getValue();
            Integer score = tuple.getScore() != null ? tuple.getScore().intValue() : 0;

            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setRank(rank++);
            entry.setUsername(username);
            entry.setScore(score);
            entry.setGameName(gameName);

            entries.add(entry);
        }

        return entries;
    }

    /**
     * Atualiza melhor score do usuário (cache)
     */
    public void updateUserBestScore(String username, Integer score) {
        String userKey = USER_BEST_SCORES_PREFIX + username;
        Integer currentBest = (Integer) redisTemplate.opsForValue().get(userKey);

        if (currentBest == null || score > currentBest) {
            redisTemplate.opsForValue().set(userKey, score);
        }
    }

    /**
     * Obtém melhor score do usuário do cache
     */
    public Integer getUserBestScore(String username) {
        String userKey = USER_BEST_SCORES_PREFIX + username;
        return (Integer) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * Verifica se os leaderboards estão sincronizados
     */
    public boolean isLeaderboardHealthy() {
        try {
            Long globalSize = getGlobalLeaderboardSize();
            return globalSize != null && globalSize >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém estatísticas dos leaderboards
     */
    public LeaderboardStats getLeaderboardStats() {
        LeaderboardStats stats = new LeaderboardStats();
        stats.setGlobalPlayersCount(getGlobalLeaderboardSize());
        stats.setRedisHealthy(isLeaderboardHealthy());

        return stats;
    }

    // Classe interna para estatísticas
    public static class LeaderboardStats {
        private Long globalPlayersCount;
        private boolean redisHealthy;

        public Long getGlobalPlayersCount() {
            return globalPlayersCount;
        }

        public void setGlobalPlayersCount(Long globalPlayersCount) {
            this.globalPlayersCount = globalPlayersCount;
        }

        public boolean isRedisHealthy() {
            return redisHealthy;
        }

        public void setRedisHealthy(boolean redisHealthy) {
            this.redisHealthy = redisHealthy;
        }
    }
}