// ScoreService.java - VERSÃO FINAL CORRIGIDA
package com.leaderboard.service;

import com.leaderboard.dto.ScoreDto;
import com.leaderboard.dto.ScoreSubmissionRequest;
import com.leaderboard.entity.Game;
import com.leaderboard.entity.Score;
import com.leaderboard.entity.User;
import com.leaderboard.exception.ResourceNotFoundException;
import com.leaderboard.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private RedisLeaderboardService redisLeaderboardService;

    public ScoreDto submitScore(String username, ScoreSubmissionRequest request) {
        User user = userService.findByUsername(username);
        Game game = gameService.findById(request.getGameId());

        // Validar se o score é válido
        if (request.getScore() < 0) {
            throw new IllegalArgumentException("Score não pode ser negativo");
        }

        // Criar novo score
        Score score = new Score(user, game, request.getScore());
        Score savedScore = scoreRepository.save(score);

        // Atualizar leaderboards no Redis
        updateLeaderboards(username, game.getId(), request.getScore());

        return convertToDto(savedScore);
    }

    private void updateLeaderboards(String username, Long gameId, Integer score) {
        try {
            redisLeaderboardService.updateGameLeaderboard(gameId, username, score);
            redisLeaderboardService.updateUserBestScore(username, score);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar Redis: " + e.getMessage());
        }
    }

    public List<ScoreDto> getUserScores(String username) {
        User user = userService.findByUsername(username);
        List<Score> scores = scoreRepository.findByUserOrderByScoreDesc(user);
        return scores.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<ScoreDto> getUserScoresForGame(String username, Long gameId) {
        User user = userService.findByUsername(username);
        Game game = gameService.findById(gameId);
        List<Score> scores = scoreRepository.findByUserAndGameOrderByScoreDesc(user, game);
        return scores.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public ScoreDto getUserBestScoreForGame(String username, Long gameId) {
        User user = userService.findByUsername(username);
        Game game = gameService.findById(gameId);

        return scoreRepository.findTopByUserAndGameOrderByScoreDesc(user, game)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Score", "user/game", username + "/" + gameId));
    }

    public Page<ScoreDto> getTopScoresGlobal(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Score> scores = scoreRepository.findTopScoresGlobal(pageable);
        return scores.map(this::convertToDto);
    }

    public Page<ScoreDto> getTopScoresByGame(Long gameId, int page, int size) {
        Game game = gameService.findById(gameId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Score> scores = scoreRepository.findTopScoresByGame(game, pageable);
        return scores.map(this::convertToDto);
    }

    public List<ScoreDto> getRecentScores(int limit) {
        List<Score> scores = scoreRepository.findTop10ByOrderBySubmittedAtDesc();
        return scores.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ScoreDto> getRecentScoresForGame(Long gameId, int limit) {
        Game game = gameService.findById(gameId);
        List<Score> scores = scoreRepository.findTop10ByGameOrderBySubmittedAtDesc(game);
        return scores.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ MÉTODO CORRIGIDO - usando o método seguro
    public boolean hasUserSubmittedToday(String username, Long gameId) {
        User user = userService.findByUsername(username);
        Game game = gameService.findById(gameId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return scoreRepository.existsByUserAndGameBetweenDates(user, game, startOfDay, endOfDay);
    }

    private ScoreDto convertToDto(Score score) {
        return new ScoreDto(
                score.getId(),
                score.getUser().getUsername(),
                score.getGame().getName(),
                score.getScore(),
                score.getSubmittedAt()
        );
    }
}