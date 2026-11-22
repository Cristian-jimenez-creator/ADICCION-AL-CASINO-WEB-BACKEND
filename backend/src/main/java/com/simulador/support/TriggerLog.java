package com.simulador.support;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("trigger_logs")
public record TriggerLog(
        @Id String id,
        String playerId,
        String situationBefore,
        String thoughtsOrFeelings,
        String urgeIntensity,
        String actionAfter,
        String helpfulness,
        String perceivedHelpfulnessOfGame,
        Instant createdAt
) {}
