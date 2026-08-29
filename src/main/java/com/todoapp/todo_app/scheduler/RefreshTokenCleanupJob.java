package com.todoapp.todo_app.scheduler;

import com.todoapp.todo_app.service.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupJob(
            RefreshTokenService refreshTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(
            cron = "0 0 3 * * *",
            zone = "UTC"
    )
    public void limpiarTokens() {
        refreshTokenService.limpiarTokens();
    }
}