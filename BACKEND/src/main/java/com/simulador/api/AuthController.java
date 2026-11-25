package com.simulador.api;

import com.simulador.accounts.AuthService;
import com.simulador.security.CurrentPlayer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints de autenticación y perfil. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    /** Tipo público RegisterBody del módulo. */
    public record RegisterBody(@NotBlank @Size(max = 80) String name,
    /** Tipo público LoginBody del módulo. */
                               @NotBlank @Email String email,
                               @NotBlank @Size(min = 8, max = 120) String password,
                               @NotBlank @Size(max = 300) String howKnown) {}
    public record LoginBody(@NotBlank @Email String email, @NotBlank String password) {}
    private final AuthService auth;
    private final CurrentPlayer current;
    private final MongoWriteConflictRetry retry;

    /** Inicializa AuthController con las dependencias necesarias. */
    public AuthController(AuthService auth, CurrentPlayer current, MongoWriteConflictRetry retry) {
        this.auth = auth;
        this.current = current;
        this.retry = retry;
    }

    /** Registra una cuenta nueva y prepara su sesión. */
    @PostMapping("/register")
    public AuthService.AuthResponse register(@Valid @RequestBody RegisterBody body) {
        return retry.execute(() -> auth.register(new AuthService.RegisterRequest(body.name(), body.email(), body.password(), body.howKnown())));
    }

    /** Valida las credenciales y crea una sesión. */
    @PostMapping("/login")
    public AuthService.AuthResponse login(@Valid @RequestBody LoginBody body) {
        return retry.execute(() -> auth.login(new AuthService.LoginRequest(body.email(), body.password())));
    }

    /** Realiza la operación pública me. */
    @GetMapping("/me")
    public AuthService.AccountView me() {
        return auth.current(current.id());
    }

    /** Realiza la operación pública activityPulse. */
    @PostMapping("/activity/pulse")
    public AuthService.AccountView activityPulse() {
        return retry.execute(() -> auth.recordActivity(current.id()));
    }

    /** Entrega el bono diario si ya corresponde. */
    @PostMapping("/daily-reward/claim")
    public AuthService.DailyRewardResponse claimDailyReward() {
        return retry.execute(() -> auth.claimDailyReward(current.id()));
    }

    /** Registra el fin del periodo de descanso. */
    @PostMapping("/rest/complete")
    public AuthService.AccountView completeRest() {
        return retry.execute(() -> auth.completeRest(current.id()));
    }
}
