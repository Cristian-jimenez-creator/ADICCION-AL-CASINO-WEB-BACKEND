package com.simulador.accounts;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class AccountService {
    public static final long DAILY_LIMIT_SECONDS = 90 * 60L;
    private static final long CYCLE_SECONDS = 24 * 60 * 60L;
    private static final long DAILY_CREDIT_GRANT = 4_000L;
    private final PlayerRepository players;

    public AccountService(PlayerRepository players) {
        this.players = players;
    }

    @Transactional
    public PlayerAccount refreshed(String playerId, Instant now) {
        PlayerAccount player = players.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del jugador no existe."));
        return players.save(refresh(player, now));
    }

    public PlayerAccount refresh(PlayerAccount player, Instant now) {
        long elapsedSeconds = Math.max(0, Duration.between(player.cycleStartedAt(), now).getSeconds());
        long cycle = elapsedSeconds / CYCLE_SECONDS;
        long grantsDue = Math.max(0, cycle - player.lastDailyGrantCycle());
        long credits = player.credits() + grantsDue * DAILY_CREDIT_GRANT;
        Instant periodStart = player.playPeriodStartedAt();
        long playedSeconds = player.activePlaySeconds();
        if (cycle > player.lastDailyGrantCycle()) {
            periodStart = player.cycleStartedAt().plusSeconds(cycle * CYCLE_SECONDS);
            playedSeconds = 0;
        }

        if (player.activeMatchId() != null && player.activeMatchCheckpoint() != null) {
            Instant from = player.activeMatchCheckpoint().isAfter(periodStart)
                    ? player.activeMatchCheckpoint() : periodStart;
            playedSeconds += Math.max(0, Duration.between(from, now).getSeconds());
        }

        Instant checkpoint = player.activeMatchId() == null ? null : now;
        Instant restUntil = player.restUntil() != null && now.isBefore(player.restUntil())
                ? player.restUntil() : null;
        return new PlayerAccount(player.id(), player.name(), player.email(), player.passwordHash(),
                player.howKnown(), player.role(), credits, player.cycleStartedAt(), cycle,
                periodStart, playedSeconds, checkpoint, player.activeMatchId(), player.createdAt(), restUntil);
    }

    @Transactional
    public PlayerAccount refreshAndSave(PlayerAccount player, Instant now) {
        return players.save(refresh(player, now));
    }

    public Optional<PlayerAccount> findByEmail(String email) {
        return players.findByEmailIgnoreCase(email.trim().toLowerCase());
    }

    public PlayerRepository repository() {
        return players;
    }

    public void requireEligibleToStart(PlayerAccount player) {
        requireEligibleToStart(player, Instant.now());
    }

    public void requireEligibleToStart(PlayerAccount player, Instant now) {
        if (player.activeMatchId() != null) {
            throw new IllegalStateException("Ya tienes una partida activa.");
        }
        if (player.activePlaySeconds() >= DAILY_LIMIT_SECONDS) {
            throw new IllegalStateException("Terminó tu tiempo de juego por este periodo de 24 horas.");
        }
        if (player.restUntil() != null && now.isBefore(player.restUntil())) {
            throw new IllegalStateException("Tu descanso de cinco minutos todavía está en curso.");
        }
    }

    @Transactional
    public PlayerAccount completeRest(String playerId, Instant now) {
        PlayerAccount player = refreshed(playerId, now);
        if (player.restUntil() == null) return player;
        return players.save(player.withRestUntil(null));
    }

    public long currentActiveSeconds(PlayerAccount player, Instant now) {
        if (player.activeMatchId() == null || player.activeMatchCheckpoint() == null) {
            return player.activePlaySeconds();
        }
        Instant from = player.activeMatchCheckpoint().isAfter(player.playPeriodStartedAt())
                ? player.activeMatchCheckpoint() : player.playPeriodStartedAt();
        return player.activePlaySeconds() + Math.max(0, Duration.between(from, now).getSeconds());
    }
}
