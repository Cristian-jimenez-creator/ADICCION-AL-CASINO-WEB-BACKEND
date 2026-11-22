package com.simulador.games;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ChallengeChatRepository extends MongoRepository<ChallengeChatMessage, String> {
    List<ChallengeChatMessage> findByChallengeIdAndCreatedAtAfterOrderByCreatedAtAsc(String challengeId, Instant after);
    List<ChallengeChatMessage> findTop100ByChallengeIdOrderByCreatedAtDesc(String challengeId);
    void deleteByChallengeId(String challengeId);
}
