package com.simulador.support;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/** Acceso de persistencia a los reportes. */
public interface PlayerReportRepository extends MongoRepository<PlayerReport, String> {
    List<PlayerReport> findAllByOrderByCreatedAtDesc();
}
