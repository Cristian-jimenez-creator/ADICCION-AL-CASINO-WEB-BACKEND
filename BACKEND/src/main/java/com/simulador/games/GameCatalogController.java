package com.simulador.games;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameCatalogController {
    public record GameOption(GameType id, String name, boolean available) {}
    private final ContentCatalog content;

    public GameCatalogController(ContentCatalog content) {
        this.content = content;
    }

    @GetMapping
    public List<GameOption> games() {
        boolean puzzles = !content.puzzles().isEmpty();
        boolean words = !content.wordPrompts().isEmpty();
        return List.of(new GameOption(GameType.COIN, "Moneda y probabilidad", true),
                new GameOption(GameType.PUZZLE, "Carrera de rompecabezas", puzzles),
                new GameOption(GameType.WORD, "Palabra o acertijo", words));
    }
}
