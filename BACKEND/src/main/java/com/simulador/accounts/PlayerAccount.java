package com.simulador.accounts;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Modelo persistente con los datos y el estado del jugador. */
@Document("players")
public record PlayerAccount(
        @Id String id,
        String name,
        @Indexed(unique = true) String email,
        String passwordHash,
        String howKnown,
        Role role,
        long credits,
        Instant cycleStartedAt,
        long lastDailyGrantCycle,
        Instant playPeriodStartedAt,
        long activePlaySeconds,
        Instant activeMatchCheckpoint,
        String activeMatchId,
        Instant createdAt,
        Instant restUntil,
        Instant activityCheckpointAt
) {
    /** Inicializa PlayerAccount con las dependencias necesarias. */
    public PlayerAccount(String id, String name, String email, String passwordHash, String howKnown,
                         Role role, long credits, Instant cycleStartedAt, long lastDailyGrantCycle,
                         Instant playPeriodStartedAt, long activePlaySeconds, Instant activeMatchCheckpoint,
                         String activeMatchId, Instant createdAt, Instant restUntil) {
        this(id, name, email, passwordHash, howKnown, role, credits, cycleStartedAt, lastDailyGrantCycle,
                playPeriodStartedAt, activePlaySeconds, activeMatchCheckpoint, activeMatchId, createdAt,
                restUntil, null);
    }

    /** Realiza la operación pública withDailyState. */
    public PlayerAccount withDailyState(long newCredits, long newGrantCycle, Instant newPeriodStart,
                                        long newActiveSeconds) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, newCredits,
                cycleStartedAt, newGrantCycle, newPeriodStart, newActiveSeconds,
                activeMatchCheckpoint, activeMatchId, createdAt, restUntil, activityCheckpointAt);
    }

    /** Realiza la operación pública withActiveMatch. */
    public PlayerAccount withActiveMatch(String matchId, Instant checkpoint) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, credits,
                cycleStartedAt, lastDailyGrantCycle, playPeriodStartedAt, activePlaySeconds,
                checkpoint, matchId, createdAt, matchId == null ? restUntil : null, activityCheckpointAt);
    }

    /** Realiza la operación pública withCredits. */
    public PlayerAccount withCredits(long newCredits) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, newCredits,
                cycleStartedAt, lastDailyGrantCycle, playPeriodStartedAt, activePlaySeconds,
                activeMatchCheckpoint, activeMatchId, createdAt, restUntil, activityCheckpointAt);
    }

    /** Realiza la operación pública withRestUntil. */
    public PlayerAccount withRestUntil(Instant until) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, credits,
                cycleStartedAt, lastDailyGrantCycle, playPeriodStartedAt, activePlaySeconds,
                activeMatchCheckpoint, activeMatchId, createdAt, until, activityCheckpointAt);
    }

    /** Realiza la operación pública withPresence. */
    public PlayerAccount withPresence(Instant checkpoint, Instant periodStart, long seconds) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, credits,
                cycleStartedAt, lastDailyGrantCycle, periodStart, seconds, activeMatchCheckpoint,
                activeMatchId, createdAt, restUntil, checkpoint);
    }

    /** Realiza la operación pública isAdmin. */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
