package com.simulador.api;

import com.simulador.games.ChallengeService;
import com.simulador.games.GameType;
import com.simulador.security.CurrentPlayer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone los endpoints para retos y partidas. */
@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    /** Tipo público CreateBody del módulo. */
    public record CreateBody(@NotNull GameType gameType, @Positive long stake) {}
    /** Tipo público JoinBody del módulo. */
    public record JoinBody(@Positive long stake) {}
    /** Tipo público CoinBody del módulo. */
    public record CoinBody(@NotBlank String side) {}
    /** Tipo público PuzzleBody del módulo. */
    public record PuzzleBody(int round, @NotNull List<String> pieces) {}
    /** Tipo público WordBody del módulo. */
    public record WordBody(int round, @NotBlank String answer) {}

    private final ChallengeService challenges;
    private final CurrentPlayer current;
    private final MongoWriteConflictRetry retry;

    /** Inicializa ChallengeController con las dependencias necesarias. */
    public ChallengeController(ChallengeService challenges, CurrentPlayer current, MongoWriteConflictRetry retry) {
        this.challenges = challenges;
        this.current = current;
        this.retry = retry;
    }

    /** Lista los retos disponibles para unirse. */
    @GetMapping("/waiting")
    public List<ChallengeService.ChallengeView> waiting() { return challenges.waitingChallenges(current.id()); }

    /** Crea un reto con el tipo de juego y apuesta solicitados. */
    @PostMapping
    public ChallengeService.ChallengeView create(@Valid @RequestBody CreateBody body) {
        return retry.execute(() -> challenges.create(current.id(), new ChallengeService.CreateChallenge(body.gameType(), body.stake())));
    }

    /** Une al jugador a un reto disponible. */
    @PostMapping("/{id}/join")
    public ChallengeService.ChallengeView join(@PathVariable String id, @Valid @RequestBody JoinBody body) {
        return retry.execute(() -> challenges.join(current.id(), id, new ChallengeService.JoinChallenge(body.stake())));
    }

    /** Cancela un reto creado por el jugador. */
    @DeleteMapping("/{id}")
    public void cancel(@PathVariable String id) { retry.execute(() -> { challenges.cancel(current.id(), id); return null; }); }

    /** Devuelve los datos actuales del jugador autenticado. */
    @GetMapping("/{id}")
    public ChallengeService.ChallengeView current(@PathVariable String id) { return retry.execute(() -> challenges.myMatch(current.id(), id)); }

    /** Registra la elección del jugador en la ronda de moneda. */
    @PostMapping("/{id}/coin")
    public ChallengeService.ChallengeView coin(@PathVariable String id, @Valid @RequestBody CoinBody body) {
        return retry.execute(() -> challenges.chooseCoinSide(current.id(), id, body.side()));
    }

    /** Registra el tablero enviado para la ronda de rompecabezas. */
    @PutMapping("/{id}/puzzle")
    public ChallengeService.ChallengeView puzzle(@PathVariable String id, @Valid @RequestBody PuzzleBody body) {
        return retry.execute(() -> challenges.submitPuzzle(current.id(), id, new ChallengeService.PuzzleSubmission(body.round(), body.pieces())));
    }

    /** Envía la respuesta del jugador a la ronda de palabras. */
    @PostMapping("/{id}/word-answer")
    public ChallengeService.WordAnswerResult word(@PathVariable String id, @Valid @RequestBody WordBody body) {
        return retry.execute(() -> challenges.submitWordAnswer(current.id(), id, new ChallengeService.WordSubmission(body.round(), body.answer())));
    }

    /** Solicita una pista para la ronda de palabras. */
    @PostMapping("/{id}/word-hint")
    public ChallengeService.ChallengeView wordHint(@PathVariable String id) {
        return retry.execute(() -> challenges.revealWordHint(current.id(), id));
    }

    /** Registra que el jugador abandona la partida. */
    @PostMapping("/{id}/forfeit")
    public ChallengeService.ChallengeView forfeit(@PathVariable String id) { return retry.execute(() -> challenges.forfeit(current.id(), id)); }
}
