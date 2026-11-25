package com.simulador.accounts;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/** Acceso de persistencia a las cuentas de jugadores. */
public interface PlayerRepository extends MongoRepository<PlayerAccount, String> {
    Optional<PlayerAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
