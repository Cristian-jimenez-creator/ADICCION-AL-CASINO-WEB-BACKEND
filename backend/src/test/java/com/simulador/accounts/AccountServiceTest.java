package com.simulador.accounts;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest {
    private final AccountService accounts = new AccountService(null);

    @Test
    void refreshingWithoutAnEntryDoesNotGrantDailyCredits() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        PlayerAccount player = player(start, 10_000, 0, null, null, 0);

        PlayerAccount refreshed = accounts.refresh(player, start.plusSeconds(86_400));

        assertEquals(10_000, refreshed.credits());
        assertEquals(0, refreshed.lastDailyGrantCycle());
        assertEquals(start.plusSeconds(86_400), refreshed.playPeriodStartedAt());
    }

    @Test
    void countsOnlyTimeFromAnActiveMatchCheckpoint() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        PlayerAccount player = player(start, 10_000, 0, start, "match-1", 30);

        PlayerAccount refreshed = accounts.refresh(player, start.plusSeconds(150));

        assertEquals(180, refreshed.activePlaySeconds());
        assertEquals(start.plusSeconds(150), refreshed.activeMatchCheckpoint());
        assertEquals("match-1", refreshed.activeMatchId());
    }

    @Test
    void preventsStartingBeforeRestEndsButAllowsStartAfterExpiry() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        PlayerAccount resting = player(now, 10_000, 0, null, null, 0).withRestUntil(now.plusSeconds(30));

        assertThrows(IllegalStateException.class, () -> accounts.requireEligibleToStart(resting, now));
        accounts.requireEligibleToStart(resting, now.plusSeconds(30));
    }

    private PlayerAccount player(Instant start, long credits, long cycle, Instant checkpoint,
                                 String activeMatchId, long activeSeconds) {
        return new PlayerAccount("player-1", "Jugador", "player@gmail.com", "hash", "recomendación",
                Role.PLAYER, credits, start, cycle, start, activeSeconds, checkpoint, activeMatchId, start, null);
    }
}
