package com.simulador.games;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChallengeTest {
    @Test
    void startingChallengeSetsPlayersAndKeepsGameFieldsEmptyUntilNeeded() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        Challenge waiting = new Challenge("challenge-1", GameType.COIN, 300, "player-a", null,
                MatchStatus.WAITING, created, null, null, null, null, null, null, null,
                0, 0, List.of(), Map.of(), null, null, null, null, 0, 0);

        Challenge active = waiting.withOpponent("player-b", created.plusSeconds(1));

        assertEquals(MatchStatus.ACTIVE, active.status());
        assertEquals("player-a", active.creatorId());
        assertEquals("player-b", active.opponentId());
        assertNull(active.puzzleContentId());
        assertEquals(created.plusSeconds(1), active.startedAt());
    }

    @Test
    void finishingRecordsWinnerAndFinishedState() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Challenge active = new Challenge("challenge-1", GameType.COIN, 300, "player-a", "player-b",
                MatchStatus.ACTIVE, now, now, null, null, null, null, null, null,
                0, 0, List.of(), Map.of(), null, null, null, null, 0, 0);

        Challenge finished = active.finish("player-b", now.plusSeconds(3));

        assertEquals(MatchStatus.FINISHED, finished.status());
        assertEquals("player-b", finished.winnerId());
        assertEquals(now.plusSeconds(3), finished.finishedAt());
    }
}
