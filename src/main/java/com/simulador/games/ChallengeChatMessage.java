package com.simulador.games;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("challenge_messages")
public record ChallengeChatMessage(
        @Id String id,
        String challengeId,
        String senderId,
        String body,
        Instant createdAt
) {}
