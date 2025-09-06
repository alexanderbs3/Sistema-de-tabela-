package com.leaderboard.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * Sincroniza leaderboards a cada hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora em milliseconds
    public void syncLeaderboards() {
        try {
            if (!leaderboardService.isRedisHealthy()) {
                System.out.println("Redis não está saudável, iniciando sincronização...");
                leaderboardService.syncLeaderboards();
            }
        } catch (Exception e) {
            System.err.println("Erro na sincronização agendada: " + e.getMessage());
        }
    }

    /**
     * Verifica health do sistema a cada 15 minutos
     */
    @Scheduled(fixedRate = 900000) // 15 minutos
    public void healthCheck() {
        try {
            LeaderboardService.LeaderboardStats stats = leaderboardService.getLeaderboardStatistics();

            if (!stats.isRedisHealthy()) {
                System.err.println("ALERTA: Redis não está funcionando corretamente!");
            }

            if (stats.isSyncNeeded()) {
                System.out.println("INFO: Sincronização necessária detectada.");
            }

            System.out.println("Health Check - Redis: " + stats.isRedisHealthy() +
                    ", Players DB: " + stats.getTotalPlayersInDb() +
                    ", Players Redis: " + stats.getGlobalPlayersInRedis());

        } catch (Exception e) {
            System.err.println("Erro no health check: " + e.getMessage());
        }
    }
}
