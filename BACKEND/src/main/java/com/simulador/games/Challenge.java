package com.simulador.games;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// Un documento conserva el reto, sus minijuegos y el marcador de rondas.
/** Modelo persistente del reto y el progreso de su partida. */
@Document("challenges")
public record Challenge(
        @Id String id,
        GameType gameType,
        long stake,
        String creatorId,
        String opponentId,
        MatchStatus status,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        String winnerId,
        String coinFirstChoice,
        String coinResult,
        String coinWinnerId,
        String puzzleContentId,
        int puzzleRows,
        int puzzleColumns,
        List<String> puzzleTarget,
        Map<String, List<String>> puzzleBoards,
        String wordContentId,
        String wordPrompt,
        String wordAnswer,
        Instant wordDeadline,
        int wordRound,
        long settlementVersion,
        String wordHint,
        int roundNumber,
        int creatorRoundWins,
        int opponentRoundWins,
        String lastRoundWinnerId
) {
    /** Keeps stored records and existing creation call sites compatible with the first best-of-three rollout. */
    public Challenge(String id, GameType gameType, long stake, String creatorId, String opponentId,
                     MatchStatus status, Instant createdAt, Instant startedAt, Instant finishedAt,
                     String winnerId, String coinFirstChoice, String coinResult, String coinWinnerId,
                     String puzzleContentId, int puzzleRows, int puzzleColumns, List<String> puzzleTarget,
                     Map<String, List<String>> puzzleBoards, String wordContentId, String wordPrompt,
                     String wordAnswer, Instant wordDeadline, int wordRound, long settlementVersion,
                     String wordHint) {
        this(id, gameType, stake, creatorId, opponentId, status, createdAt, startedAt, finishedAt,
                winnerId, coinFirstChoice, coinResult, coinWinnerId, puzzleContentId, puzzleRows,
                puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt, wordAnswer,
                wordDeadline, wordRound, settlementVersion, wordHint, 1, 0, 0, null);
    }

    /** Inicializa Challenge con las dependencias necesarias. */
    public Challenge(String id, GameType gameType, long stake, String creatorId, String opponentId,
                     MatchStatus status, Instant createdAt, Instant startedAt, Instant finishedAt,
                     String winnerId, String coinFirstChoice, String coinResult, String coinWinnerId,
                     String puzzleContentId, int puzzleRows, int puzzleColumns, List<String> puzzleTarget,
                     Map<String, List<String>> puzzleBoards, String wordContentId, String wordPrompt,
                     String wordAnswer, Instant wordDeadline, int wordRound, long settlementVersion) {
        this(id, gameType, stake, creatorId, opponentId, status, createdAt, startedAt, finishedAt,
                winnerId, coinFirstChoice, coinResult, coinWinnerId, puzzleContentId, puzzleRows,
                puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt, wordAnswer,
                wordDeadline, wordRound, settlementVersion, null);
    }

    /** Crea una copia del reto con el segundo participante. */
    public Challenge withOpponent(String playerId, Instant start) {
        return new Challenge(id, gameType, stake, creatorId, playerId, MatchStatus.ACTIVE,
                createdAt, start, null, null, null, null, null, null, 0, 0, List.of(), Map.of(),
                null, null, null, null, 0, settlementVersion, null, 1, 0, 0, null);
    }

    /** Cancela un reto creado por el jugador. */
    public Challenge cancel(Instant at) {
        return copy(MatchStatus.CANCELLED, at, winnerId, coinFirstChoice, coinResult, coinWinnerId, puzzleContentId,
                puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt,
                wordAnswer, wordDeadline, wordRound, settlementVersion + 1, wordHint,
                roundNumber(), creatorRoundWins, opponentRoundWins, lastRoundWinnerId);
    }

    /** Crea una copia del reto con la elección de moneda. */
    public Challenge withCoinChoice(String choice) {
        return copy(status, finishedAt, winnerId, choice, coinResult, coinWinnerId, puzzleContentId,
                puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt,
                wordAnswer, wordDeadline, wordRound, settlementVersion, wordHint,
                roundNumber(), creatorRoundWins, opponentRoundWins, lastRoundWinnerId);
    }

    /** Crea una copia con el resultado de la ronda de moneda. */
    public Challenge withCoinOutcome(String result, String roundWinner) {
        return copy(status, finishedAt, winnerId, null, result, roundWinner, puzzleContentId,
                puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt,
                wordAnswer, wordDeadline, wordRound, settlementVersion, wordHint,
                roundNumber(), creatorRoundWins, opponentRoundWins, roundWinner);
    }

    /** Crea una copia con el rompecabezas asignado. */
    public Challenge withPuzzle(List<String> target, Map<String, List<String>> boards,
                                int rows, int columns, String contentId) {
        return copy(status, finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId, contentId,
                rows, columns, target, boards, wordContentId, wordPrompt, wordAnswer, wordDeadline,
                wordRound, settlementVersion, wordHint, roundNumber(), creatorRoundWins,
                opponentRoundWins, lastRoundWinnerId);
    }

    /** Actualiza el tablero enviado por un participante. */
    public Challenge withPuzzleBoard(String playerId, List<String> board) {
        var updated = new java.util.HashMap<>(puzzleBoards == null ? Map.<String, List<String>>of() : puzzleBoards);
        updated.put(playerId, List.copyOf(board));
        return copy(status, finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId,
                puzzleContentId, puzzleRows, puzzleColumns, puzzleTarget, Map.copyOf(updated),
                wordContentId, wordPrompt, wordAnswer, wordDeadline, wordRound, settlementVersion,
                wordHint, roundNumber(), creatorRoundWins, opponentRoundWins, lastRoundWinnerId);
    }

    /** Actualiza el marcador y el ganador de la ronda. */
    public Challenge withRoundScore(int round, int creatorWins, int opponentWins, String roundWinner) {
        return copy(MatchStatus.ACTIVE, null, null, null, coinResult, coinWinnerId, puzzleContentId,
                puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt,
                wordAnswer, wordDeadline, wordRound, settlementVersion, wordHint, round,
                creatorWins, opponentWins, roundWinner);
    }

    /** Marca el reto como finalizado con su resultado. */
    public Challenge finish(String winner, Instant at) {
        return copy(MatchStatus.FINISHED, at, winner, coinFirstChoice, coinResult, coinWinnerId,
                puzzleContentId, puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, wordContentId,
                wordPrompt, wordAnswer, wordDeadline, wordRound, settlementVersion + 1, wordHint,
                roundNumber(), creatorRoundWins, opponentRoundWins, lastRoundWinnerId);
    }

    /** Crea una copia con la ronda y el plazo de respuesta. */
    public Challenge withWordRound(String contentId, String prompt, String answer, Instant deadline, int round) {
        return copy(status, finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId,
                puzzleContentId, puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, contentId,
                prompt, answer, deadline, round, settlementVersion, null, roundNumber(),
                creatorRoundWins, opponentRoundWins, lastRoundWinnerId);
    }

    /** Crea una copia con la pista visible para los participantes. */
    public Challenge withWordHint(String hint) {
        return copy(status, finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId,
                puzzleContentId, puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, wordContentId,
                wordPrompt, wordAnswer, wordDeadline, wordRound, settlementVersion, hint,
                roundNumber(), creatorRoundWins, opponentRoundWins, lastRoundWinnerId);
    }

    /** Devuelve el número de ronda actual. */
    public int roundNumber() {
        return roundNumber < 1 ? 1 : roundNumber;
    }

    private Challenge copy(MatchStatus nextStatus, Instant nextFinishedAt, String nextWinner,
                           String nextCoinChoice, String nextCoinResult, String nextCoinWinner,
                           String nextPuzzleContentId, int nextPuzzleRows, int nextPuzzleColumns,
                           List<String> nextPuzzleTarget, Map<String, List<String>> nextPuzzleBoards,
                           String nextWordContentId, String nextWordPrompt, String nextWordAnswer,
                           Instant nextWordDeadline, int nextWordRound, long nextSettlementVersion,
                           String nextWordHint, int nextRoundNumber, int nextCreatorWins,
                           int nextOpponentWins, String nextLastRoundWinner) {
        return new Challenge(id, gameType, stake, creatorId, opponentId, nextStatus, createdAt,
                startedAt, nextFinishedAt, nextWinner, nextCoinChoice, nextCoinResult, nextCoinWinner,
                nextPuzzleContentId, nextPuzzleRows, nextPuzzleColumns, nextPuzzleTarget,
                nextPuzzleBoards, nextWordContentId, nextWordPrompt, nextWordAnswer, nextWordDeadline,
                nextWordRound, nextSettlementVersion, nextWordHint, nextRoundNumber, nextCreatorWins,
                nextOpponentWins, nextLastRoundWinner);
    }
}
