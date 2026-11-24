package com.simulador.games;

import com.simulador.accounts.AccountService;
import com.simulador.accounts.PlayerAccount;
import com.simulador.accounts.PlayerRepository;
import com.simulador.support.PlayerBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ChallengeService {
    public static final List<Long> ALLOWED_STAKES = List.of(100L, 300L, 800L, 2_000L);
    public record CreateChallenge(GameType gameType, long stake) {}
    public record JoinChallenge(long stake) {}
    public record PuzzleSubmission(int round, List<String> pieces) {
        public PuzzleSubmission(List<String> pieces) { this(1, pieces); }
    }
    public record WordSubmission(int round, String answer) {}
    public record WordAnswerResult(boolean correct, boolean roundExpired, ChallengeView challenge) {}
    public record ChallengeView(String id, GameType gameType, long stake, String creatorId,
                                String creatorName, String opponentId, String opponentName,
                                MatchStatus status, Instant createdAt, Instant startedAt, Instant finishedAt,
                                String winnerId, String coinFirstChoice, String coinResult, String coinWinnerId,
                                String puzzleTitle, String puzzleImageUrl, int puzzleRows, int puzzleColumns,
                                Map<String, Integer> puzzlePieceSourceIndices,
                                List<String> myPuzzleBoard, List<String> opponentPuzzleBoard,
                                String wordPrompt, int wordRound, Instant wordDeadline, int wordLength,
                                String wordHint, int roundNumber, int creatorRoundWins,
                                int opponentRoundWins, String lastRoundWinnerId) {}

    private final ChallengeRepository challenges;
    private final PlayerRepository players;
    private final AccountService accounts;
    private final ContentCatalog content;
    private final PlayerBlockRepository blocks;
    private final java.security.SecureRandom random = new java.security.SecureRandom();
    private final ChallengeChatRepository messages;

    public ChallengeService(ChallengeRepository challenges, PlayerRepository players,
                            AccountService accounts, ContentCatalog content,
                            PlayerBlockRepository blocks, ChallengeChatRepository messages) {
        this.challenges = challenges;
        this.players = players;
        this.accounts = accounts;
        this.content = content;
        this.blocks = blocks;
        this.messages = messages;
    }

    @Transactional
    public ChallengeView create(String creatorId, CreateChallenge request) {
        validateStake(request.stake());
        PlayerAccount creator = accounts.refreshed(creatorId, Instant.now());
        accounts.requireEligibleToStart(creator);
        if (creator.credits() < request.stake()) throw new IllegalStateException("No tienes suficientes créditos para esa apuesta.");
        validateContentAvailable(request.gameType());
        Instant now = Instant.now();
        cancelWaitingOffers(creatorId, null, now);
        Challenge challenge = new Challenge(null, request.gameType(), request.stake(), creatorId,
                null, MatchStatus.WAITING, now, null, null, null, null, null, null,
                null, 0, 0, List.of(), Map.of(), null, null, null, null, 0, 0);
        return view(challenges.save(challenge), creatorId);
    }

    public List<ChallengeView> waitingChallenges(String viewerId) {
        Set<String> creatorsSeen = new HashSet<>();
        return challenges.findByStatusOrderByCreatedAtDesc(MatchStatus.WAITING).stream()
                .filter(challenge -> !challenge.creatorId().equals(viewerId))
                .filter(challenge -> !blocks.existsByBlockerIdAndBlockedId(viewerId, challenge.creatorId()))
                .filter(challenge -> !blocks.existsByBlockerIdAndBlockedId(challenge.creatorId(), viewerId))
                .filter(challenge -> creatorsSeen.add(challenge.creatorId()))
                .map(challenge -> view(challenge, viewerId)).toList();
    }

    @Transactional
    // Valida a ambos jugadores, descuenta la apuesta y prepara el minijuego.
    public ChallengeView join(String playerId, String challengeId, JoinChallenge request) {
        Challenge challenge = challenges.findByIdAndStatus(challengeId, MatchStatus.WAITING)
                .orElseThrow(() -> new IllegalStateException("El reto ya no está disponible."));
        if (challenge.creatorId().equals(playerId)) throw new IllegalArgumentException("No puedes unirte a tu propio reto.");
        if (request.stake() != challenge.stake()) throw new IllegalArgumentException("Debes unirte con la misma apuesta del reto.");
        if (blocks.existsByBlockerIdAndBlockedId(playerId, challenge.creatorId())
                || blocks.existsByBlockerIdAndBlockedId(challenge.creatorId(), playerId)) {
            throw new IllegalStateException("No puedes unirte a este reto.");
        }

        Instant now = Instant.now();
        cancelWaitingOffers(challenge.creatorId(), challenge.id(), now);
        PlayerAccount creator = accounts.refreshed(challenge.creatorId(), now);
        PlayerAccount opponent = accounts.refreshed(playerId, now);
        accounts.requireEligibleToStart(creator);
        accounts.requireEligibleToStart(opponent);
        if (creator.credits() < challenge.stake() || opponent.credits() < challenge.stake()) {
            throw new IllegalStateException("Uno de los jugadores ya no tiene créditos suficientes para iniciar esta partida.");
        }

        Challenge active = challenge.withOpponent(playerId, now);
        if (active.gameType() == GameType.PUZZLE) active = startPuzzle(active);
        if (active.gameType() == GameType.WORD) active = startWordRound(active, 1, now);

        players.save(creator.withCredits(creator.credits() - challenge.stake()).withActiveMatch(active.id(), now));
        players.save(opponent.withCredits(opponent.credits() - challenge.stake()).withActiveMatch(active.id(), now));
        return view(challenges.save(active), playerId);
    }

    @Transactional
    public void cancel(String playerId, String challengeId) {
        Challenge challenge = challenges.findByIdAndStatus(challengeId, MatchStatus.WAITING)
                .orElseThrow(() -> new IllegalStateException("El reto ya no está esperando."));
        if (!challenge.creatorId().equals(playerId)) throw new IllegalStateException("Solo quien publicó el reto puede cancelarlo.");
        challenges.save(new Challenge(challenge.id(), challenge.gameType(), challenge.stake(), challenge.creatorId(),
                null, MatchStatus.CANCELLED, challenge.createdAt(), null, Instant.now(), null,
                null, null, null, null, 0, 0, List.of(), Map.of(), null, null, null, null, 0,
                challenge.settlementVersion() + 1));
    }

    @Transactional
    public ChallengeView myMatch(String playerId, String challengeId) {
        Challenge challenge = requireParticipant(playerId, challengeId);
        if (challenge.status() == MatchStatus.ACTIVE && challenge.gameType() == GameType.WORD
                && challenge.wordDeadline() != null && !Instant.now().isBefore(challenge.wordDeadline())) {
            challenge = completeRound(challenge, null, Instant.now());
        }
        return view(challenge, playerId);
    }

    private void cancelWaitingOffers(String creatorId, String exceptChallengeId, Instant now) {
        challenges.findByCreatorIdAndStatusOrderByCreatedAtDesc(creatorId, MatchStatus.WAITING).stream()
                .filter(challenge -> exceptChallengeId == null || !challenge.id().equals(exceptChallengeId))
                .forEach(challenge -> challenges.save(challenge.cancel(now)));
    }

    @Transactional
    public ChallengeView chooseCoinSide(String playerId, String challengeId, String side) {
        Challenge challenge = requireActive(playerId, challengeId, GameType.COIN);
        if (challenge.coinFirstChoice() != null) throw new IllegalStateException("Ya se eligió un lado para esta partida.");
        String choice = side == null ? "" : side.trim().toUpperCase(Locale.ROOT);
        if (!choice.equals("CARA") && !choice.equals("SELLO")) throw new IllegalArgumentException("Elige cara o sello.");

        String result = random.nextBoolean() ? "CARA" : "SELLO";
        String winner = choice.equals(result) ? playerId : otherPlayer(challenge, playerId);
        challenge = challenge.withCoinChoice(choice).withCoinOutcome(result, winner);
        challenge = completeRound(challenge, winner, Instant.now());
        return view(challenge, playerId);
    }

    @Transactional
    public ChallengeView submitPuzzle(String playerId, String challengeId, PuzzleSubmission submission) {
        Challenge challenge = requireActive(playerId, challengeId, GameType.PUZZLE);
        if (submission.round() != challenge.roundNumber()) return view(challenge, playerId);
        if (submission.pieces() == null || !isValidPermutation(submission.pieces(), challenge.puzzleTarget())) {
            throw new IllegalArgumentException("El tablero enviado no contiene las piezas válidas del rompecabezas.");
        }
        challenge = challenge.withPuzzleBoard(playerId, submission.pieces());
        if (submission.pieces().equals(challenge.puzzleTarget())) {
            challenge = completeRound(challenge, playerId, Instant.now());
        } else {
            challenges.save(challenge);
        }
        return view(challenge, playerId);
    }

    @Transactional
    public WordAnswerResult submitWordAnswer(String playerId, String challengeId, WordSubmission submission) {
        Challenge challenge = requireActive(playerId, challengeId, GameType.WORD);
        Instant now = Instant.now();
        if (challenge.wordDeadline() == null || !now.isBefore(challenge.wordDeadline())) {
            challenge = completeRound(challenge, null, now);
            return new WordAnswerResult(false, true, view(challenge, playerId));
        }
        if (submission.round() != challenge.wordRound()) {
            return new WordAnswerResult(false, true, view(challenge, playerId));
        }
        if (normalizeAnswer(submission.answer()).equals(normalizeAnswer(challenge.wordAnswer()))) {
            challenge = completeRound(challenge, playerId, now);
            return new WordAnswerResult(true, false, view(challenge, playerId));
        }
        return new WordAnswerResult(false, false, view(challenge, playerId));
    }

    @Transactional
    public ChallengeView revealWordHint(String playerId, String challengeId) {
        Challenge challenge = requireActive(playerId, challengeId, GameType.WORD);
        if (challenge.wordHint() == null || challenge.wordHint().isBlank()) {
            String wordContentId = challenge.wordContentId();
            WordContent current = content.wordPrompts().stream()
                    .filter(value -> value.id().equals(wordContentId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No se encontró la pista de esta ronda."));
            challenge = challenges.save(challenge.withWordHint(current.hint()));
        }
        return view(challenge, playerId);
    }

    @Transactional
    public ChallengeView forfeit(String playerId, String challengeId) {
        Challenge challenge = requireParticipant(playerId, challengeId);
        if (challenge.status() != MatchStatus.ACTIVE) throw new IllegalStateException("La partida no está activa.");
        String winner = otherPlayer(challenge, playerId);
        int creatorWins = winner.equals(challenge.creatorId()) ? 2 : challenge.creatorRoundWins();
        int opponentWins = winner.equals(challenge.opponentId()) ? 2 : challenge.opponentRoundWins();
        Instant now = Instant.now();
        challenge = challenge.withRoundScore(challenge.roundNumber(), creatorWins, opponentWins, winner)
                .finish(winner, now);
        challenge = settleWinner(challenge, winner, now);
        return view(challenge, playerId);
    }

    public Challenge requireParticipant(String playerId, String challengeId) {
        Challenge challenge = challenges.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la partida."));
        if (!isParticipant(challenge, playerId)) throw new IllegalStateException("No perteneces a esta partida.");
        return challenge;
    }

    public boolean isBlocked(String firstPlayerId, String secondPlayerId) {
        return blocks.existsByBlockerIdAndBlockedId(firstPlayerId, secondPlayerId)
                || blocks.existsByBlockerIdAndBlockedId(secondPlayerId, firstPlayerId);
    }

    private Challenge requireActive(String playerId, String challengeId, GameType type) {
        Challenge challenge = requireParticipant(playerId, challengeId);
        if (challenge.status() != MatchStatus.ACTIVE) throw new IllegalStateException("La partida ya terminó.");
        if (challenge.gameType() != type) throw new IllegalArgumentException("Esta acción no corresponde al juego de la partida.");
        return challenge;
    }

    private Challenge startPuzzle(Challenge challenge) {
        PuzzleContent puzzle = content.choosePuzzle();
        List<String> creatorBoard = new ArrayList<>(puzzle.targetPieces());
        List<String> opponentBoard = new ArrayList<>(puzzle.targetPieces());
        do { Collections.shuffle(creatorBoard); } while (creatorBoard.equals(puzzle.targetPieces()));
        do { Collections.shuffle(opponentBoard); } while (opponentBoard.equals(puzzle.targetPieces()));
        Map<String, List<String>> boards = new HashMap<>();
        boards.put(challenge.creatorId(), List.copyOf(creatorBoard));
        boards.put(challenge.opponentId(), List.copyOf(opponentBoard));
        return challenge.withPuzzle(puzzle.targetPieces(), boards, puzzle.rows(), puzzle.columns(), puzzle.id());
    }

    private Challenge startWordRound(Challenge challenge, int round, Instant now) {
        WordContent prompt = content.chooseWordPrompt();
        return challenge.withWordRound(prompt.id(), prompt.prompt(), prompt.answer(), now.plusSeconds(40), round);
    }

    // Cierra la ronda y comprueba si el reto al mejor de tres ya terminó.
    private Challenge completeRound(Challenge challenge, String roundWinnerId, Instant now) {
        int creatorWins = challenge.creatorRoundWins() + (challenge.creatorId().equals(roundWinnerId) ? 1 : 0);
        int opponentWins = challenge.opponentRoundWins() + (challenge.opponentId().equals(roundWinnerId) ? 1 : 0);
        int completedRound = challenge.roundNumber();
        challenge = challenge.withRoundScore(completedRound, creatorWins, opponentWins, roundWinnerId);

        String winner = creatorWins >= 2 ? challenge.creatorId()
                : opponentWins >= 2 ? challenge.opponentId()
                : completedRound >= 3 && creatorWins != opponentWins
                ? (creatorWins > opponentWins ? challenge.creatorId() : challenge.opponentId()) : null;
        if (winner != null) return settleWinner(challenge.finish(winner, now), winner, now);
        if (completedRound >= 3) return settleDraw(challenge.finish(null, now), now);

        Challenge next = challenge.withRoundScore(completedRound + 1, creatorWins, opponentWins, roundWinnerId);
        if (next.gameType() == GameType.PUZZLE) next = startPuzzle(next);
        if (next.gameType() == GameType.WORD) next = startWordRound(next, completedRound + 1, now);
        return challenges.save(next);
    }

    // Entrega el pozo al ganador y libera a los jugadores.
    private Challenge settleWinner(Challenge challenge, String winnerId, Instant now) {
        PlayerAccount creator = accounts.refreshed(challenge.creatorId(), now);
        PlayerAccount opponent = accounts.refreshed(challenge.opponentId(), now);
        PlayerAccount winner = winnerId.equals(creator.id()) ? creator : opponent;
        long prize = challenge.stake() * 2;
        players.save(winner.withCredits(winner.credits() + prize).withActiveMatch(null, null)
                .withRestUntil(now.plusSeconds(5 * 60L)));
        PlayerAccount loser = winnerId.equals(creator.id()) ? opponent : creator;
        players.save(loser.withActiveMatch(null, null).withRestUntil(now.plusSeconds(5 * 60L)));
        challenges.save(challenge);
        messages.deleteByChallengeId(challenge.id());
        return challenge;
    }

    private Challenge settleDraw(Challenge challenge, Instant now) {
        PlayerAccount creator = accounts.refreshed(challenge.creatorId(), now);
        PlayerAccount opponent = accounts.refreshed(challenge.opponentId(), now);
        players.save(creator.withCredits(creator.credits() + challenge.stake()).withActiveMatch(null, null)
                .withRestUntil(now.plusSeconds(5 * 60L)));
        players.save(opponent.withCredits(opponent.credits() + challenge.stake()).withActiveMatch(null, null)
                .withRestUntil(now.plusSeconds(5 * 60L)));
        challenges.save(challenge);
        messages.deleteByChallengeId(challenge.id());
        return challenge;
    }

    private ChallengeView view(Challenge challenge, String viewerId) {
        PlayerAccount creator = players.findById(challenge.creatorId()).orElse(null);
        PlayerAccount opponent = challenge.opponentId() == null ? null : players.findById(challenge.opponentId()).orElse(null);
        PuzzleContent puzzle = null;
        if (challenge.puzzleContentId() != null) {
            puzzle = content.puzzles().stream().filter(value -> value.id().equals(challenge.puzzleContentId())).findFirst().orElse(null);
        }
        Map<String, List<String>> boards = challenge.puzzleBoards() == null ? Map.of() : challenge.puzzleBoards();
        Map<String, Integer> sourceIndices = new HashMap<>();
        List<String> targetPieces = challenge.puzzleTarget() == null ? List.of() : challenge.puzzleTarget();
        for (int index = 0; index < targetPieces.size(); index++) sourceIndices.put(targetPieces.get(index), index);
        String otherId = challenge.creatorId().equals(viewerId) ? challenge.opponentId() : challenge.creatorId();
        return new ChallengeView(challenge.id(), challenge.gameType(), challenge.stake(), challenge.creatorId(),
                creator == null ? "Jugador" : creator.name(), challenge.opponentId(),
                opponent == null ? null : opponent.name(), challenge.status(), challenge.createdAt(),
                challenge.startedAt(), challenge.finishedAt(), challenge.winnerId(), challenge.coinFirstChoice(),
                challenge.coinResult(), challenge.coinWinnerId(), puzzle == null ? null : puzzle.title(),
                puzzle == null ? null : puzzle.imageUrl(), challenge.puzzleRows(), challenge.puzzleColumns(),
                Map.copyOf(sourceIndices), boards.get(viewerId), otherId == null ? null : boards.get(otherId),
                challenge.wordPrompt(), challenge.wordRound(), challenge.wordDeadline(), wordLength(challenge.wordAnswer()),
                challenge.wordHint(), challenge.roundNumber(), challenge.creatorRoundWins(),
                challenge.opponentRoundWins(), challenge.lastRoundWinnerId());
    }

    private int wordLength(String answer) {
        if (answer == null) return 0;
        return (int) answer.codePoints().filter(Character::isLetterOrDigit).count();
    }

    private boolean isParticipant(Challenge challenge, String playerId) {
        return challenge.creatorId().equals(playerId) || playerId.equals(challenge.opponentId());
    }

    private String otherPlayer(Challenge challenge, String playerId) {
        return challenge.creatorId().equals(playerId) ? challenge.opponentId() : challenge.creatorId();
    }

    private void validateContentAvailable(GameType gameType) {
        if (gameType == null) throw new IllegalArgumentException("Selecciona un juego.");
        if (gameType == GameType.PUZZLE && content.puzzles().isEmpty()) {
            throw new IllegalStateException("El contenido de rompecabezas aún no está configurado.");
        }
        if (gameType == GameType.WORD && content.wordPrompts().isEmpty()) {
            throw new IllegalStateException("El contenido de palabra y acertijo aún no está configurado.");
        }
    }

    private void validateStake(long stake) {
        if (!ALLOWED_STAKES.contains(stake)) throw new IllegalArgumentException("La apuesta debe ser 100, 300, 800 o 2.000 créditos.");
    }

    private boolean isValidPermutation(List<String> submitted, List<String> target) {
        return submitted.size() == target.size() && submitted.size() == submitted.stream().distinct().count()
                && submitted.stream().allMatch(target::contains);
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        String decomposed = Normalizer.normalize(answer.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        return decomposed.replaceAll("[^\\p{L}\\p{N}]", "");
    }
}
