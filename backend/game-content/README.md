# Contenido de juegos

El banco incluye rompecabezas ilustrados de animales y lugares conocidos, además de acertijos en español. Se puede ampliar o reemplazar editando los archivos TSV. La moneda y probabilidad no depende de este catálogo.

## `puzzles.tsv`

Un rompecabezas por línea, con campos separados por tabulador:

`id<TAB>título<TAB>URL_de_imagen<TAB>filas<TAB>columnas<TAB>pieza1,pieza2,...`

Filas y columnas deben ser al menos 2. La cantidad de identificadores de pieza debe ser igual a filas × columnas y no pueden repetirse. Los identificadores indican el orden correcto de las piezas. La imagen debe poder cargarse desde el navegador.

## `word-prompts.tsv`

Un reto por línea:

`id<TAB>enunciado<TAB>respuesta`

Las respuestas del banco son palabras en español; el juego usa su longitud para mostrar un espacio por letra.
