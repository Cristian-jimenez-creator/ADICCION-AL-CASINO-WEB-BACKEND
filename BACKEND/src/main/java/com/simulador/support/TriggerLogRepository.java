package com.simulador.support;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/** Acceso de persistencia a los registros de desencadenantes. */
public interface TriggerLogRepository extends MongoRepository<TriggerLog, String> {
    List<TriggerLog> findByPlayerIdOrderByCreatedAtDesc(String playerId);
}
