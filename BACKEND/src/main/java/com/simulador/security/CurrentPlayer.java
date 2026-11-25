package com.simulador.security;

import com.simulador.accounts.AccountService;
import com.simulador.accounts.PlayerAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Obtiene y valida al jugador autenticado actual. */
@Component
public class CurrentPlayer {
    private final AccountService accounts;

    /** Inicializa CurrentPlayer con las dependencias necesarias. */
    public CurrentPlayer(AccountService accounts) {
        this.accounts = accounts;
    }

    /** Devuelve la identidad autenticada de la solicitud actual. */
    public AppPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppPrincipal principal)) {
            throw new IllegalStateException("Debes iniciar sesión.");
        }
        return principal;
    }

    /** Realiza la operación pública refresh. */
    public PlayerAccount refresh() {
        return accounts.refreshed(principal().id(), Instant.now());
    }

    /** Devuelve el identificador del jugador autenticado. */
    public String id() {
        return principal().id();
    }

    /** Exige el rol administrativo para continuar. */
    public void requireAdmin() {
        if (!principal().role().name().equals("ADMIN")) throw new AccessDeniedException("Esta vista requiere una cuenta de administrador.");
    }
}
