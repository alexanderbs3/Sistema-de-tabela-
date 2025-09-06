package com.leaderboard.exception;

public class LeaderboardException extends RuntimeException{
    public LeaderboardException(String message) {
        super(message);
    }

    public LeaderboardException(String message, Throwable cause) {
        super(message, cause);
    }
}
