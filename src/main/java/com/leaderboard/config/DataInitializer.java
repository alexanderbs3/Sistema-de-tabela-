package com.leaderboard.config;

import com.leaderboard.entity.Game;
import com.leaderboard.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private GameRepository gameRepository;

    @Override
    public void run(String... args) throws Exception {
        // Verificar se já existem jogos
        if (gameRepository.count() == 0) {
            // Criar jogos padrão
            gameRepository.save(new Game("Snake Game", "Clássico jogo da cobrinha"));
            gameRepository.save(new Game("Tetris", "Jogo de blocos em queda"));
            gameRepository.save(new Game("Pac-Man", "Come-come dos fantasmas"));
            gameRepository.save(new Game("Space Invaders", "Defenda a Terra dos invasores"));
            gameRepository.save(new Game("Frogger", "Atravesse a rua e o rio"));

            System.out.println("✅ Jogos iniciais criados com sucesso!");
        }
    }
}