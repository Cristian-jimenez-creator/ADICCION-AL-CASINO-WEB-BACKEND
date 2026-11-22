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

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    public record CreateBody(@NotNull GameType gameType, @Positive long stake) {}
    public record JoinBody(@Positive long stake) {}
    public record CoinBody(@NotBlank String side) {}
    public record PuzzleBody(@NotNull List<String> pieces) {}
    public record WordBody(int round, @NotBlank String answer) {}

    private final ChallengeService challenges;
    private final CurrentPlayer current;

    public ChallengeController(ChallengeService challenges, CurrentPlayer current) {
        this.challenges = challenges;
        this.current = current;
    }

    @GetMapping("/waiting")
    public List<ChallengeService.ChallengeView> waiting() { return challenges.waitingChallenges(current.id()); }

    @PostMapping
    public ChallengeService.ChallengeView create(@Valid @RequestBody CreateBody body) {
        return challenges.create(current.id(), new ChallengeService.CreateChallenge(body.gameType(), body.stake()));
    }

    @PostMapping("/{id}/join")
    public ChallengeService.ChallengeView join(@PathVariable String id, @Valid @RequestBody JoinBody body) {
        return challenges.join(current.id(), id, new ChallengeService.JoinChallenge(body.stake()));
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable String id) { challenges.cancel(current.id(), id); }

    @GetMapping("/{id}")
    public ChallengeService.ChallengeView current(@PathVariable String id) { return challenges.myMatch(current.id(), id); }

    @PostMapping("/{id}/coin")
    public ChallengeService.ChallengeView coin(@PathVariable String id, @Valid @RequestBody CoinBody body) {
        return challenges.chooseCoinSide(current.id(), id, body.side());
    }

    @PutMapping("/{id}/puzzle")
    public ChallengeService.ChallengeView puzzle(@PathVariable String id, @Valid @RequestBody PuzzleBody body) {
        return challenges.submitPuzzle(current.id(), id, new ChallengeService.PuzzleSubmission(body.pieces()));
    }

    @PostMapping("/{id}/word-answer")
    public ChallengeService.WordAnswerResult word(@PathVariable String id, @Valid @RequestBody WordBody body) {
        return challenges.submitWordAnswer(current.id(), id, new ChallengeService.WordSubmission(body.round(), body.answer()));
    }

    @PostMapping("/{id}/word-hint")
    public ChallengeService.ChallengeView wordHint(@PathVariable String id) {
        return challenges.revealWordHint(current.id(), id);
    }

    @PostMapping("/{id}/forfeit")
    public ChallengeService.ChallengeView forfeit(@PathVariable String id) { return challenges.forfeit(current.id(), id); }
}
