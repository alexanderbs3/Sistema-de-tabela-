package com.leaderboard.controller;

import com.leaderboard.dto.ApiResponse;
import com.leaderboard.dto.LeaderboardEntry;
import com.leaderboard.dto.UserRankingDto;
import com.leaderboard.security.UserPrincipal;
import com.leaderboard.service.LeaderboardService;
import com.leaderboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LeaderboardController {

    // ✅ CORREÇÃO: Adicionar final
    private final LeaderboardService leaderboardService;
    private final UserService userService;

    @GetMapping("/public/global")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<LeaderboardEntry> leaderboard = leaderboardService.getGlobalLeaderboard(limit);
            return ResponseEntity.ok(ApiResponse.success("Leaderboard global carregado", leaderboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar leaderboard: " + e.getMessage()));
        }
    }

    @GetMapping("/my/ranking")
    public ResponseEntity<ApiResponse<UserRankingDto>> getMyRanking(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            UserRankingDto ranking = userService.getUserRanking(userPrincipal.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Seu ranking carregado", ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar seu ranking: " + e.getMessage()));
        }
    }

    @GetMapping("/my/global-rank")
    public ResponseEntity<ApiResponse<Long>> getMyGlobalRank(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long rank = leaderboardService.getUserGlobalRank(userPrincipal.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Sua posição global", rank));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar posição global: " + e.getMessage()));
        }
    }

    @GetMapping("/my/game-rank/{gameId}")
    public ResponseEntity<ApiResponse<Long>> getMyGameRank(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameId) {
        try {
            Long rank = leaderboardService.getUserGameRank(gameId, userPrincipal.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Sua posição no jogo", rank));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar posição no jogo: " + e.getMessage()));
        }
    }

    @GetMapping("/range/global")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getGlobalRankRange(
            @RequestParam int start,
            @RequestParam int end) {
        try {
            if (start < 1 || end < start || (end - start) > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Parâmetros inválidos. Start deve ser >= 1, end >= start, e range <= 100"));
            }

            List<LeaderboardEntry> entries = leaderboardService.getGlobalRankRange(start, end);
            return ResponseEntity.ok(ApiResponse.success("Faixa do ranking global", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar faixa do ranking: " + e.getMessage()));
        }
    }

    @GetMapping("/range/game/{gameId}")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getGameRankRange(
            @PathVariable Long gameId,
            @RequestParam int start,
            @RequestParam int end) {
        try {
            if (start < 1 || end < start || (end - start) > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Parâmetros inválidos. Start deve ser >= 1, end >= start, e range <= 100"));
            }

            List<LeaderboardEntry> entries = leaderboardService.getGameRankRange(gameId, start, end);
            return ResponseEntity.ok(ApiResponse.success("Faixa do ranking do jogo", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar faixa do ranking do jogo: " + e.getMessage()));
        }
    }

    @GetMapping("/top-performers")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getTopPerformersInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<LeaderboardEntry> entries = leaderboardService.getTopPerformersInPeriod(startDate, endDate, limit);
            return ResponseEntity.ok(ApiResponse.success("Top performers do período", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar top performers: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/sync")
    public ResponseEntity<ApiResponse<String>> syncLeaderboards() {
        try {
            leaderboardService.syncLeaderboards();
            return ResponseEntity.ok(ApiResponse.success("Sincronização iniciada com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao iniciar sincronização: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/status")
    public ResponseEntity<ApiResponse<LeaderboardService.LeaderboardStats>> getLeaderboardStatus() {
        try {
            LeaderboardService.LeaderboardStats stats = leaderboardService.getLeaderboardStatistics();
            return ResponseEntity.ok(ApiResponse.success("Status dos leaderboards", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao obter status: " + e.getMessage()));
        }
    }
}