package com.simulador.voice;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/** Acceso de persistencia a las señales de voz. */
public interface RtcSignalRepository extends MongoRepository<RtcSignal, String> {
    List<RtcSignal> findByChallengeIdAndRecipientIdAndCreatedAtAfterOrderByCreatedAtAsc(
            String challengeId, String recipientId, Instant after);
}
