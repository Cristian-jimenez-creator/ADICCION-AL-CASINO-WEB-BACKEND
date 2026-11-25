package com.simulador.support;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Acceso de persistencia a los bloqueos. */
public interface PlayerBlockRepository extends MongoRepository<PlayerBlock, String> {
    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);
    void deleteByBlockerIdAndBlockedId(String blockerId, String blockedId);
}
