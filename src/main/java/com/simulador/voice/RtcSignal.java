package com.simulador.voice;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("rtc_signals")
public record RtcSignal(
        @Id String id,
        String challengeId,
        String senderId,
        String recipientId,
        String kind,
        String payload,
        @Indexed(expireAfter = "PT10M") Instant createdAt
) {}
