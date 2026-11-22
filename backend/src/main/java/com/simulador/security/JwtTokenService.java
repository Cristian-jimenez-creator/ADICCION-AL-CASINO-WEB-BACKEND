package com.simulador.security;

import com.simulador.accounts.PlayerAccount;
import com.simulador.accounts.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtTokenService {
    private static final String HEADER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private final byte[] secret;

    public JwtTokenService(@Value("${app.jwt.secret:local-only-change-this-secret-before-deploying-0001}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("APP_JWT_SECRET debe tener al menos 32 bytes.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(PlayerAccount player) {
        long expires = Instant.now().plus(Duration.ofHours(12)).getEpochSecond();
        String body = player.id() + "|" + player.email() + "|" + player.role().name() + "|" + expires;
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(body.getBytes(StandardCharsets.UTF_8));
        String content = HEADER + "." + payload;
        return content + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sign(content));
    }

    public AppPrincipal verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !HEADER.equals(parts[0])) return null;
            String content = parts[0] + "." + parts[1];
            byte[] providedSignature = Base64.getUrlDecoder().decode(parts[2]);
            if (!java.security.MessageDigest.isEqual(sign(content), providedSignature)) return null;
            String body = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String[] fields = body.split("\\|", -1);
            if (fields.length != 4 || Long.parseLong(fields[3]) <= Instant.now().getEpochSecond()) return null;
            return new AppPrincipal(fields[0], fields[1], Role.valueOf(fields[2]));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar la sesión.", ex);
        }
    }
}
