package com.simulador.security;

import com.simulador.accounts.Role;

/** Identidad del jugador autenticado en una sesión. */
public record AppPrincipal(String id, String email, Role role) {}
