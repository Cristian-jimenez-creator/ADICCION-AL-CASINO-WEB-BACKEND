package com.simulador.games;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Carga y selecciona el contenido de rompecabezas y palabras. */
@Component
public class ContentCatalog {
    private final Path directory;

    /** Inicializa ContentCatalog con las dependencias necesarias. */
    public ContentCatalog(@Value("${app.game-content-dir:./game-content}") String directory) {
        this.directory = Path.of(directory);
    }

    /** Devuelve el contenido de rompecabezas disponible. */
    public List<PuzzleContent> puzzles() {
        return readLines("puzzles.tsv").stream().map(line -> {
            String[] fields = line.split("\\t", -1);
            if (fields.length != 6 || fields[0].isBlank() || fields[1].isBlank() || fields[2].isBlank()) {
                throw new IllegalStateException("Fila inválida en puzzles.tsv: faltan id, título o URL de imagen.");
            }
            List<String> target = Arrays.stream(fields[5].split(","))
                    .map(String::trim).filter(value -> !value.isEmpty()).toList();
            int rows = Integer.parseInt(fields[3]);
            int columns = Integer.parseInt(fields[4]);
            if (rows < 2 || columns < 2 || rows * columns != target.size() || target.size() != target.stream().distinct().count()) {
                throw new IllegalStateException("El rompecabezas " + fields[0] + " no tiene una cuadrícula válida.");
            }
            return new PuzzleContent(fields[0], fields[1], fields[2], rows, columns, target);
        }).toList();
    }

    /** Devuelve las rondas de palabras disponibles. */
    public List<WordContent> wordPrompts() {
        return readLines("word-prompts.tsv").stream().map(line -> {
            String[] fields = line.split("\\t", 4);
            if (fields.length < 3 || fields[2].isBlank()) {
                throw new IllegalStateException("Fila inválida en word-prompts.tsv.");
            }
            String hint = fields.length == 4 && !fields[3].isBlank()
                    ? fields[3] : "La respuesta comienza por «" + fields[2].substring(0, 1).toUpperCase() + "».";
            return new WordContent(fields[0], fields[1], fields[2], hint);
        }).toList();
    }

    /** Selecciona un rompecabezas para una nueva partida. */
    public PuzzleContent choosePuzzle() {
        List<PuzzleContent> values = puzzles();
        if (values.isEmpty()) throw new IllegalStateException("El banco de rompecabezas aún no está configurado.");
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    /** Selecciona una ronda de palabras para una nueva partida. */
    public WordContent chooseWordPrompt() {
        List<WordContent> values = wordPrompts();
        if (values.isEmpty()) throw new IllegalStateException("El banco de palabras y acertijos aún no está configurado.");
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private List<String> readLines(String filename) {
        Path file = directory.resolve(filename);
        if (!Files.exists(file)) return List.of();
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                    .map(String::trim).filter(line -> !line.isBlank() && !line.startsWith("#")).toList();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer " + file.toAbsolutePath(), ex);
        }
    }
}
