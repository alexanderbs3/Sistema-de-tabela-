package com.leaderboard.repository;

import com.leaderboard.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT g FROM Game g ORDER BY g.createdAt DESC")
    List<Game> findAllOrderByCreatedAtDesc();

    @Query("""
        SELECT g, 
               (SELECT COUNT(DISTINCT s.user) FROM Score s WHERE s.game = g) as totalPlayers,
               (SELECT COUNT(s) FROM Score s WHERE s.game = g) as totalScores
        FROM Game g
        """)
    List<Object[]> findAllGamesWithStats();


}
