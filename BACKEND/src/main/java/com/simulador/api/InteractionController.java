package com.simulador.api;

import com.simulador.games.*;
import com.simulador.security.CurrentPlayer;
import com.simulador.support.*;
import com.simulador.voice.RtcSignal;
import com.simulador.voice.RtcSignalRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InteractionController {
    public record MessageBody(@NotBlank @Size(max = 1000) String body) {}
    public record ReportBody(@NotBlank String channel) {}
    public record BlockBody(@NotBlank String playerId) {}
    public record TriggerBody(@Size(max=1000) String situationBefore, @Size(max=1000) String thoughtsOrFeelings,
                              @Size(max=100) String urgeIntensity, @Size(max=1000) String actionAfter,
                              @Size(max=1000) String helpfulness, @Size(max=1000) String perceivedHelpfulnessOfGame) {}
    public record SignalBody(@NotBlank String kind, @NotBlank @Size(max=20000) String payload) {}
    public record MessageView(String id, String senderId, String body, Instant createdAt) {}

    private final CurrentPlayer current;
    private final ChallengeService challenges;
    private final ChallengeChatRepository messages;
    private final PlayerReportRepository reports;
    private final PlayerBlockRepository blocks;
    private final TriggerLogRepository triggerLogs;
    private final RtcSignalRepository signals;

    public InteractionController(CurrentPlayer current, ChallengeService challenges, ChallengeChatRepository messages,
                                 PlayerReportRepository reports, PlayerBlockRepository blocks,
                                 TriggerLogRepository triggerLogs, RtcSignalRepository signals) {
        this.current=current; this.challenges=challenges; this.messages=messages; this.reports=reports;
        this.blocks=blocks; this.triggerLogs=triggerLogs; this.signals=signals;
    }

    @GetMapping("/challenges/{id}/messages")
    public List<MessageView> messages(@PathVariable String id, @RequestParam(required=false) Instant after) {
        var match=challenges.requireParticipant(current.id(), id);
        if (match.status()!=MatchStatus.ACTIVE) return List.of();
        if (challenges.isBlocked(match.creatorId(), match.opponentId())) return List.of();
        List<ChallengeChatMessage> found = after == null
                ? messages.findTop100ByChallengeIdOrderByCreatedAtDesc(id)
                : messages.findByChallengeIdAndCreatedAtAfterOrderByCreatedAtAsc(id, after);
        return found.stream().sorted(java.util.Comparator.comparing(ChallengeChatMessage::createdAt))
                .map(m -> new MessageView(m.id(),m.senderId(),m.body(),m.createdAt())).toList();
    }

    @PostMapping("/challenges/{id}/messages")
    public MessageView send(@PathVariable String id, @Valid @RequestBody MessageBody body) {
        var match=challenges.requireParticipant(current.id(), id);
        if (match.status()!=MatchStatus.ACTIVE) throw new IllegalStateException("El chat solo está disponible durante la partida.");
        String recipient=match.creatorId().equals(current.id())?match.opponentId():match.creatorId();
        if (challenges.isBlocked(current.id(),recipient)) throw new IllegalStateException("La interacción está bloqueada.");
        var saved=messages.save(new ChallengeChatMessage(null,id,current.id(),body.body().trim(),Instant.now()));
        return new MessageView(saved.id(),saved.senderId(),saved.body(),saved.createdAt());
    }

    @PostMapping("/challenges/{id}/reports")
    public Map<String,String> report(@PathVariable String id,@Valid @RequestBody ReportBody body) {
        var match=challenges.requireParticipant(current.id(),id);
        String other=match.creatorId().equals(current.id())?match.opponentId():match.creatorId();
        if (!(body.channel().equalsIgnoreCase("TEXT")||body.channel().equalsIgnoreCase("VOICE")))
            throw new IllegalArgumentException("El canal debe ser TEXT o VOICE.");
        reports.save(new PlayerReport(null,current.id(),other,id,body.channel().toUpperCase(),Instant.now()));
        return Map.of("status","received");
    }

    @PostMapping("/players/block")
    public Map<String,String> block(@Valid @RequestBody BlockBody body) {
        if (current.id().equals(body.playerId())) throw new IllegalArgumentException("No puedes bloquear tu propia cuenta.");
        if (!blocks.existsByBlockerIdAndBlockedId(current.id(),body.playerId()))
            blocks.save(new PlayerBlock(null,current.id(),body.playerId(),Instant.now()));
        return Map.of("status","blocked");
    }

    @DeleteMapping("/players/block/{playerId}")
    public Map<String,String> unblock(@PathVariable String playerId) {
        blocks.deleteByBlockerIdAndBlockedId(current.id(),playerId);
        return Map.of("status","unblocked");
    }

    @PostMapping("/trigger-logs")
    public TriggerLog saveTrigger(@Valid @RequestBody TriggerBody body) {
        return triggerLogs.save(new TriggerLog(null,current.id(),body.situationBefore(),body.thoughtsOrFeelings(),
                body.urgeIntensity(),body.actionAfter(),body.helpfulness(),body.perceivedHelpfulnessOfGame(),Instant.now()));
    }

    @GetMapping("/trigger-logs")
    public List<TriggerLog> myTriggers() { return triggerLogs.findByPlayerIdOrderByCreatedAtDesc(current.id()); }

    @GetMapping("/challenges/{id}/voice-signals")
    public List<RtcSignal> voiceSignals(@PathVariable String id,@RequestParam(required=false) Instant after) {
        var match=challenges.requireParticipant(current.id(),id);
        String recipient=match.creatorId().equals(current.id())?match.opponentId():match.creatorId();
        if(match.status()!=MatchStatus.ACTIVE || challenges.isBlocked(current.id(),recipient)) return List.of();
        return signals.findByChallengeIdAndRecipientIdAndCreatedAtAfterOrderByCreatedAtAsc(id,current.id(),
                after==null?Instant.EPOCH:after);
    }

    @PostMapping("/challenges/{id}/voice-signals")
    public RtcSignal voiceSignal(@PathVariable String id,@Valid @RequestBody SignalBody body) {
        var match=challenges.requireParticipant(current.id(),id);
        String recipient=match.creatorId().equals(current.id())?match.opponentId():match.creatorId();
        if(match.status()!=MatchStatus.ACTIVE || challenges.isBlocked(current.id(),recipient))
            throw new IllegalStateException("La voz no está disponible para esta partida.");
        if(!List.of("OFFER","ANSWER","ICE").contains(body.kind().toUpperCase()))
            throw new IllegalArgumentException("Tipo de señal de voz no admitido.");
        return signals.save(new RtcSignal(null,id,current.id(),recipient,body.kind().toUpperCase(),body.payload(),Instant.now()));
    }
}
