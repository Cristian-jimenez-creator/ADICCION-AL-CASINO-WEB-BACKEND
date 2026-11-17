package com.simulador.accounts;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

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
        Instant restUntil
) {
    public PlayerAccount withDailyState(long newCredits, long newGrantCycle, Instant newPeriodStart,
                                        long newActiveSeconds) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, newCredits,
                cycleStartedAt, newGrantCycle, newPeriodStart, newActiveSeconds,
                activeMatchCheckpoint, activeMatchId, createdAt, restUntil);
    }

    public PlayerAccount withActiveMatch(String matchId, Instant checkpoint) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, credits,
                cycleStartedAt, lastDailyGrantCycle, playPeriodStartedAt, activePlaySeconds,
                checkpoint, matchId, createdAt, matchId == null ? restUntil : null);
    }

    public PlayerAccount withCredits(long newCredits) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, newCredits,
                cycleStartedAt, lastDailyGrantCycle, playPeriodStartedAt, activePlaySeconds,
                activeMatchCheckpoint, activeMatchId, createdAt, restUntil);
    }

    public PlayerAccount withRestUntil(Instant until) {
        return new PlayerAccount(id, name, email, passwordHash, howKnown, role, credits,
                cycleStartedAt, lastDailyGrantCycle, playPeriodStartedAt, activePlaySeconds,
                activeMatchCheckpoint, activeMatchId, createdAt, until);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
