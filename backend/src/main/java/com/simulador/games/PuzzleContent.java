package com.simulador.games;

import java.util.List;

public record PuzzleContent(String id, String title, String imageUrl, int rows, int columns,
                            List<String> targetPieces) {}
