package com.leaderboard.service;

import com.leaderboard.dto.UserRankingDto;
import com.leaderboard.entity.User;
import com.leaderboard.exception.ResourceNotFoundException;
import com.leaderboard.repository.ScoreRepository;
import com.leaderboard.repository.UserRepository;
import com.leaderboard.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private RedisLeaderboardService redisLeaderboardService;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usernameOrEmail));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserRankingDto getUserRanking(String username) {
        User user = findByUsername(username);

        // Buscar estatísticas do usuário no banco
        Object[] stats = scoreRepository.findUserStatistics(user);

        Integer bestScore = stats[0] != null ? ((Number) stats[0]).intValue() : 0;
        Integer totalScores = stats[1] != null ? ((Number) stats[1]).intValue() : 0;
        Double averageScore = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;

        // Buscar ranking do Redis
        Long globalRank = redisLeaderboardService.getUserGlobalRank(username);

        return new UserRankingDto(username, globalRank, bestScore, totalScores, averageScore);
    }

    public User getCurrentUser(UserPrincipal userPrincipal) {
        return findById(userPrincipal.getId());
    }
}
