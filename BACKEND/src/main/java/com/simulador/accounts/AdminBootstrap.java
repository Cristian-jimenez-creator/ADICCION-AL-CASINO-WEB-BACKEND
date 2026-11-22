package com.simulador.accounts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner createConfiguredAdmin(PlayerRepository players, PasswordEncoder passwords,
                                             @Value("${app.admin.email:}") String email,
                                             @Value("${app.admin.password:}") String password) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.length() < 12) {
                throw new IllegalStateException("Configura APP_ADMIN_EMAIL y una APP_ADMIN_PASSWORD de al menos 12 caracteres.");
            }
            String normalized = AuthService.normalizeEmail(email);
            players.findByEmailIgnoreCase(normalized).ifPresentOrElse(existing -> {
                if (!existing.isAdmin()) throw new IllegalStateException("APP_ADMIN_EMAIL ya pertenece a un jugador.");
            }, () -> {
                Instant now = Instant.now();
                players.save(new PlayerAccount(null, "Administrador", normalized, passwords.encode(password),
                        "Cuenta administrativa configurada para el proyecto", Role.ADMIN, 0,
                        now, 0, now, 0, null, null, now, null));
            });
        };
    }
}
