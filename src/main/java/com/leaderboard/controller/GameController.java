package com.leaderboard.controller;

import com.leaderboard.dto.ApiResponse;
import com.leaderboard.dto.GameDto;
import com.leaderboard.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class GameController {

    // ✅ CORREÇÃO: Adicionar final para funcionar com @RequiredArgsConstructor
    private final GameService gameService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<GameDto>>> getAllGames() {
        try {
            List<GameDto> games = gameService.findAll();
            return ResponseEntity.ok(ApiResponse.success("Jogos carregados com sucesso", games));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erro ao carregar jogos: " + e.getMessage()));
        }
    }
}