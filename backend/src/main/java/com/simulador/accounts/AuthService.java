package com.simulador.accounts;

import com.simulador.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {
    public record RegisterRequest(String name, String email, String password, String howKnown) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token, AccountView player, long dailyCreditsAwarded) {}
    public record DailyRewardResponse(AccountView player, long dailyCreditsAwarded) {}
    public record AccountView(String id, String name, String email, long credits,
                              long activePlaySeconds, long remainingPlaySeconds, String role,
                              Instant restUntil) {}

    private final PlayerRepository players;
    private final PasswordEncoder passwords;
    private final JwtTokenService tokens;
    private final AccountService accounts;

    public AuthService(PlayerRepository players, PasswordEncoder passwords, JwtTokenService tokens,
                       AccountService accounts) {
        this.players = players;
        this.passwords = passwords;
        this.tokens = tokens;
        this.accounts = accounts;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        validateGmail(email);
        if (players.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese Gmail.");
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        Instant now = Instant.now();
        PlayerAccount created = new PlayerAccount(null, required(request.name(), "nombre"), email,
                passwords.encode(request.password()), required(request.howKnown(), "cómo conoció el juego"),
                Role.PLAYER, 10_000, now, 0, now, 0, null, null, now, null);
        PlayerAccount saved = players.save(created);
        return new AuthResponse(tokens.issue(saved), view(saved, now), 0);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        PlayerAccount player = players.findByEmailIgnoreCase(email)
                .filter(value -> passwords.matches(request.password() == null ? "" : request.password(), value.passwordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Gmail o contraseña incorrectos."));
        Instant now = Instant.now();
        AccountService.DailyReward reward = accounts.claimDailyRewardOnEntry(player.id(), now);
        player = reward.player();
        return new AuthResponse(tokens.issue(player), view(player, now), reward.creditsAwarded());
    }

    @Transactional
    public AccountView current(String playerId) {
        Instant now = Instant.now();
        return view(accounts.refreshed(playerId, now), now);
    }

    @Transactional
    public DailyRewardResponse claimDailyReward(String playerId) {
        Instant now = Instant.now();
        AccountService.DailyReward reward = accounts.claimDailyRewardOnEntry(playerId, now);
        return new DailyRewardResponse(view(reward.player(), now), reward.creditsAwarded());
    }

    @Transactional
    public AccountView recordActivity(String playerId) {
        Instant now = Instant.now();
        return view(accounts.recordPresence(playerId, now), now);
    }

    @Transactional
    public AccountView completeRest(String playerId) {
        Instant now = Instant.now();
        return view(accounts.completeRest(playerId, now), now);
    }

    public static AccountView view(PlayerAccount player, Instant now) {
        long active = player.activePlaySeconds();
        if (player.activeMatchId() != null && player.activeMatchCheckpoint() != null) {
            Instant from = player.activeMatchCheckpoint().isAfter(player.playPeriodStartedAt())
                    ? player.activeMatchCheckpoint() : player.playPeriodStartedAt();
            active += Math.max(0, java.time.Duration.between(from, now).getSeconds());
        }
        return new AccountView(player.id(), player.name(), player.email(), player.credits(), active,
                Math.max(0, AccountService.DAILY_LIMIT_SECONDS - active), player.role().name(), player.restUntil());
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateGmail(String email) {
        if (!email.matches("^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@gmail\\.com$")) {
            throw new IllegalArgumentException("Ingresa una dirección de Gmail válida.");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("El campo " + field + " es obligatorio.");
        return value.trim();
    }
}
