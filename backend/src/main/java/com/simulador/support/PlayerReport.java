package com.simulador.support;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("player_reports")
public record PlayerReport(
        @Id String id,
        String reporterId,
        String reportedPlayerId,
        String challengeId,
        String channel,
        Instant createdAt
) {}
