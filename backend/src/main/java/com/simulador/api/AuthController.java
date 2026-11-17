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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegisterBody(@NotBlank @Size(max = 80) String name,
                               @NotBlank @Email String email,
                               @NotBlank @Size(min = 8, max = 120) String password,
                               @NotBlank @Size(max = 300) String howKnown) {}
    public record LoginBody(@NotBlank @Email String email, @NotBlank String password) {}
    private final AuthService auth;
    private final CurrentPlayer current;

    public AuthController(AuthService auth, CurrentPlayer current) {
        this.auth = auth;
        this.current = current;
    }

    @PostMapping("/register")
    public AuthService.AuthResponse register(@Valid @RequestBody RegisterBody body) {
        return auth.register(new AuthService.RegisterRequest(body.name(), body.email(), body.password(), body.howKnown()));
    }

    @PostMapping("/login")
    public AuthService.AuthResponse login(@Valid @RequestBody LoginBody body) {
        return auth.login(new AuthService.LoginRequest(body.email(), body.password()));
    }

    @GetMapping("/me")
    public AuthService.AccountView me() {
        return auth.current(current.id());
    }

    @PostMapping("/rest/complete")
    public AuthService.AccountView completeRest() {
        return auth.completeRest(current.id());
    }
}
