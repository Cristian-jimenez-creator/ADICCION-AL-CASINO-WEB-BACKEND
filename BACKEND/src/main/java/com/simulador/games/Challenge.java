package com.simulador.games;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
        long settlementVersion
) {
    public Challenge withOpponent(String playerId, Instant start) {
        return new Challenge(id, gameType, stake, creatorId, playerId, MatchStatus.ACTIVE,
                createdAt, start, null, null, null, null, null, null, 0, 0, List.of(), Map.of(),
                null, null, null, null, 0, settlementVersion);
    }

    public Challenge withCoinChoice(String choice) {
        return new Challenge(id, gameType, stake, creatorId, opponentId, status, createdAt, startedAt,
                finishedAt, winnerId, choice, coinResult, coinWinnerId, puzzleContentId, puzzleRows,
                puzzleColumns, puzzleTarget, puzzleBoards, wordContentId, wordPrompt, wordAnswer,
                wordDeadline, wordRound, settlementVersion);
    }

    public Challenge withCoinResult(String result, String winner) {
        return new Challenge(id, gameType, stake, creatorId, opponentId, MatchStatus.FINISHED,
                createdAt, startedAt, Instant.now(), winner, coinFirstChoice, result, winner,
                puzzleContentId, puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards,
                wordContentId, wordPrompt, wordAnswer, wordDeadline, wordRound, settlementVersion + 1);
    }

    public Challenge withPuzzle(List<String> target, Map<String, List<String>> boards,
                                int rows, int columns, String contentId) {
        return new Challenge(id, gameType, stake, creatorId, opponentId, status, createdAt, startedAt,
                finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId, contentId, rows,
                columns, target, boards, wordContentId, wordPrompt, wordAnswer, wordDeadline, wordRound,
                settlementVersion);
    }

    public Challenge withPuzzleBoard(String playerId, List<String> board) {
        var updated = new java.util.HashMap<>(puzzleBoards);
        updated.put(playerId, List.copyOf(board));
        return new Challenge(id, gameType, stake, creatorId, opponentId, status, createdAt, startedAt,
                finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId, puzzleContentId,
                puzzleRows, puzzleColumns, puzzleTarget, Map.copyOf(updated), wordContentId,
                wordPrompt, wordAnswer, wordDeadline, wordRound, settlementVersion);
    }

    public Challenge finish(String winner, Instant at) {
        return new Challenge(id, gameType, stake, creatorId, opponentId, MatchStatus.FINISHED,
                createdAt, startedAt, at, winner, coinFirstChoice, coinResult, coinWinnerId,
                puzzleContentId, puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards,
                wordContentId, wordPrompt, wordAnswer, wordDeadline, wordRound, settlementVersion + 1);
    }

    public Challenge withWordRound(String contentId, String prompt, String answer, Instant deadline, int round) {
        return new Challenge(id, gameType, stake, creatorId, opponentId, status, createdAt, startedAt,
                finishedAt, winnerId, coinFirstChoice, coinResult, coinWinnerId, puzzleContentId,
                puzzleRows, puzzleColumns, puzzleTarget, puzzleBoards, contentId, prompt, answer,
                deadline, round, settlementVersion);
    }
}
