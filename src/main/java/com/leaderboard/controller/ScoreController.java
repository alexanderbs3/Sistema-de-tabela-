package com.leaderboard.controller;

import com.leaderboard.dto.ApiResponse;
import com.leaderboard.dto.ScoreDto;
import com.leaderboard.dto.ScoreSubmissionRequest;
import com.leaderboard.security.UserPrincipal;
import com.leaderboard.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*",maxAge = 3600)
@RequiredArgsConstructor

public class ScoreController {
    private ScoreService scoreService;


    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<ScoreDto>> submitScore(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ScoreSubmissionRequest request) {
        try {
            ScoreDto score = scoreService.submitScore(userPrincipal.getUsername(), request);
            return ResponseEntity.ok(ApiResponse.success("Score submetido com sucesso", score));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao submeter score: " + e.getMessage()));
        }
    }
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ScoreDto>>> getMyScores(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<ScoreDto> scores = scoreService.getUserScores(userPrincipal.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Seus scores carregados com sucesso", scores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar scores: " + e.getMessage()));
        }
    }

    @GetMapping("/my/game/{gameId}")
    public ResponseEntity<ApiResponse<List<ScoreDto>>> getMyScoresForGame(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameId) {
        try {
            List<ScoreDto> scores = scoreService.getUserScoresForGame(userPrincipal.getUsername(), gameId);
            return ResponseEntity.ok(ApiResponse.success("Seus scores para o jogo carregados com sucesso", scores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar scores do jogo: " + e.getMessage()));
        }
    }

    @GetMapping("/my/best/game/{gameId}")
    public ResponseEntity<ApiResponse<ScoreDto>> getMyBestScoreForGame(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameId) {
        try {
            ScoreDto score = scoreService.getUserBestScoreForGame(userPrincipal.getUsername(), gameId);
            return ResponseEntity.ok(ApiResponse.success("Seu melhor score para o jogo", score));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Score não encontrado: " + e.getMessage()));
        }
    }

    @GetMapping("/top/global")
    public ResponseEntity<ApiResponse<Page<ScoreDto>>> getTopGlobalScores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ScoreDto> scores = scoreService.getTopScoresGlobal(page, size);
            return ResponseEntity.ok(ApiResponse.success("Top scores globais carregados", scores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar top scores: " + e.getMessage()));
        }
    }

    @GetMapping("/top/game/{gameId}")
    public ResponseEntity<ApiResponse<Page<ScoreDto>>> getTopGameScores(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ScoreDto> scores = scoreService.getTopScoresByGame(gameId, page, size);
            return ResponseEntity.ok(ApiResponse.success("Top scores do jogo carregados", scores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar top scores do jogo: " + e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ScoreDto>>> getRecentScores(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ScoreDto> scores = scoreService.getRecentScores(limit);
            return ResponseEntity.ok(ApiResponse.success("Scores recentes carregados", scores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar scores recentes: " + e.getMessage()));
        }
    }

    @GetMapping("/recent/game/{gameId}")
    public ResponseEntity<ApiResponse<List<ScoreDto>>> getRecentGameScores(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ScoreDto> scores = scoreService.getRecentScoresForGame(gameId, limit);
            return ResponseEntity.ok(ApiResponse.success("Scores recentes do jogo carregados", scores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar scores recentes do jogo: " + e.getMessage()));
        }
    }
}
