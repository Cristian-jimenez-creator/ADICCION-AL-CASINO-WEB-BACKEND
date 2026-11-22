package com.simulador.support;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlayerReportRepository extends MongoRepository<PlayerReport, String> {
    List<PlayerReport> findAllByOrderByCreatedAtDesc();
}
