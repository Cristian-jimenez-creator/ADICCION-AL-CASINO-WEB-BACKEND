package com.simulador.games;

import java.util.List;

/** Contenido de una imagen y su tablero de rompecabezas. */
public record PuzzleContent(String id, String title, String imageUrl, int rows, int columns,
                            List<String> targetPieces) {}
