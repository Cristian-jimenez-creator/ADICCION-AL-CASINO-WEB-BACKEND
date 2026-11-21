# Contenido de juegos

El banco inicial incluye dos rompecabezas geométricos con imágenes SVG embebidas y cinco acertijos en español. Se pueden ampliar o reemplazar editando los archivos TSV. La moneda y probabilidad no depende de este catálogo.

## `puzzles.tsv`

Un rompecabezas por línea, con campos separados por tabulador:

`id<TAB>título<TAB>URL_de_imagen<TAB>filas<TAB>columnas<TAB>pieza1,pieza2,...`

Filas y columnas deben ser al menos 2. La cantidad de identificadores de pieza debe ser igual a filas × columnas y no pueden repetirse. Los identificadores indican el orden correcto de las piezas. La imagen debe poder cargarse desde el navegador.

## `word-prompts.tsv`

Un reto por línea:

`id<TAB>enunciado<TAB>respuesta`

No se agregan preguntas ni respuestas de ejemplo para evitar inventar contenido del proyecto.
