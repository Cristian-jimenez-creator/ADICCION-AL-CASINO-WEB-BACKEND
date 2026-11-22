package com.simulador.games;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends MongoRepository<Challenge, String> {
    List<Challenge> findByStatusOrderByCreatedAtAsc(MatchStatus status);
    List<Challenge> findByStatusOrderByCreatedAtDesc(MatchStatus status);
    List<Challenge> findByCreatorIdAndStatusOrderByCreatedAtDesc(String creatorId, MatchStatus status);
    List<Challenge> findByCreatorIdOrOpponentIdOrderByCreatedAtDesc(String creatorId, String opponentId);
    Optional<Challenge> findByIdAndStatus(String id, MatchStatus status);
}

