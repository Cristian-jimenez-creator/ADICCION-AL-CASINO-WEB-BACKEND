package com.simulador.support;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TriggerLogRepository extends MongoRepository<TriggerLog, String> {
    List<TriggerLog> findByPlayerIdOrderByCreatedAtDesc(String playerId);
}
