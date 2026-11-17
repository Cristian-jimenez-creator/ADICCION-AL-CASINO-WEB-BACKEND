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

@Component
public class ContentCatalog {
    private final Path directory;

    public ContentCatalog(@Value("${app.game-content-dir:./game-content}") String directory) {
        this.directory = Path.of(directory);
    }

    public List<PuzzleContent> puzzles() {
        return readLines("puzzles.tsv").stream().map(line -> {
            String[] fields = line.split("\\t", -1);
            if (fields.length != 6) throw new IllegalStateException("Fila inválida en puzzles.tsv.");
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

    public List<WordContent> wordPrompts() {
        return readLines("word-prompts.tsv").stream().map(line -> {
            String[] fields = line.split("\\t", 3);
            if (fields.length != 3 || fields[2].isBlank()) {
                throw new IllegalStateException("Fila inválida en word-prompts.tsv.");
            }
            return new WordContent(fields[0], fields[1], fields[2]);
        }).toList();
    }

    public PuzzleContent choosePuzzle() {
        List<PuzzleContent> values = puzzles();
        if (values.isEmpty()) throw new IllegalStateException("El banco de rompecabezas aún no está configurado.");
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

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
