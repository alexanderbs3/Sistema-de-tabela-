package com.leaderboard.service;

import com.leaderboard.dto.GameDto;
import com.leaderboard.entity.Game;
import com.leaderboard.exception.ResourceNotFoundException;
import com.leaderboard.repository.GameRepository;
import com.leaderboard.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    // ✅ CORREÇÃO: Adicionar final para funcionar com @RequiredArgsConstructor
    private final GameRepository gameRepository;
    private final ScoreRepository scoreRepository;

    public List<GameDto> findAll() {
        List<Game> games = gameRepository.findAllOrderByCreatedAtDesc();
        return games.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Game findById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "id", id));
    }

    public Game findByName(String name) {
        return gameRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo", "name", name));
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    public GameDto getGameWithStats(Long id) {
        Game game = findById(id);
        GameDto dto = convertToDto(game);

        // Adicionar estatísticas
        dto.setTotalPlayers(scoreRepository.countDistinctPlayersByGame(game).intValue());
        dto.setTotalScores(game.getScores().size());

        return dto;
    }

    private GameDto convertToDto(Game game) {
        return new GameDto(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getCreatedAt()
        );
    }
}