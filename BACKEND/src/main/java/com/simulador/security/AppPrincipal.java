package com.simulador.security;

import com.simulador.accounts.Role;

public record AppPrincipal(String id, String email, Role role) {}
