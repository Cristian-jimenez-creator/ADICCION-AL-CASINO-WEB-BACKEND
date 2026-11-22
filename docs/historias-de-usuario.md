# Historias de usuario — Simulador web con créditos ficticios

## Base y criterio

- **Base:** requisitos confirmados por la persona solicitante en la conversación y consolidados en la especificación del proyecto.
- **Criterio:** las historias describen únicamente comportamientos confirmados. Las decisiones que aún faltan se señalan como **Por definir**; no se presuponen tecnologías, resultados clínicos ni reglas adicionales.
- **Alcance económico:** las apuestas usan créditos ficticios; el proyecto no contempla dinero real.

## Épica 1: Registro y créditos

### HU-01 — Registrarme

**Como** persona que quiere usar el simulador, **quiero** crear una cuenta con mi nombre, cuenta de Gmail, contraseña y cómo conocí el juego, **para** ingresar a la página como jugador.

**Criterios de aceptación**

- El formulario solicita nombre, cuenta de Gmail, contraseña y cómo conoció el juego.
- Cuando la persona completa el registro, el sistema crea su cuenta.
- Las reglas de verificación del correo y recuperación de contraseña están **Por definir**.

### HU-02 — Recibir créditos al primer ingreso

**Como** jugador nuevo, **quiero** recibir 10.000 créditos ficticios al ingresar por primera vez, **para** poder participar en los juegos.

**Criterios de aceptación**

- En el primer ingreso del jugador, el sistema acredita 10.000 créditos ficticios.
- El saldo de créditos no representa dinero real.

### HU-03 — Recibir créditos diarios

**Como** jugador, **quiero** recibir 4.000 créditos ficticios cada 24 horas desde el segundo día, **para** contar con créditos para jugar.

**Criterios de aceptación**

- El periodo de 24 horas comienza con el primer ingreso del jugador.
- Desde el segundo día, el sistema agrega 4.000 créditos al saldo existente.
- Los créditos se acumulan y no se ha definido un saldo máximo.

### HU-04 — Consultar mi saldo

**Como** jugador, **quiero** que mi saldo refleje las asignaciones diarias y el resultado de mis partidas, **para** saber cuántos créditos ficticios tengo.

**Criterios de aceptación**

- El saldo conserva las asignaciones diarias acumuladas.
- El saldo refleja créditos apostados, ganados o perdidos según las reglas confirmadas de la partida.

## Épica 2: Retos y partidas

### HU-05 — Elegir un juego y una apuesta

**Como** jugador, **quiero** seleccionar uno de los tres juegos y una apuesta disponible, **para** crear o aceptar un reto.

**Criterios de aceptación**

- La página ofrece moneda y probabilidad, carrera de rompecabezas y palabra/acertijo.
- Las apuestas disponibles son 300, 800 y 2.000 créditos.

### HU-06 — Publicar un reto

**Como** jugador, **quiero** publicar un reto con el juego y la cantidad de créditos elegidos, **para** que otro jugador pueda unirse.

**Criterios de aceptación**

- El reto visible indica el juego y el importe de la apuesta.
- El reto queda disponible para que otro jugador pueda verlo y unirse con la misma apuesta.
- Si todavía no hay rival, el reto puede permanecer esperando.

### HU-07 — Unirme a un reto

**Como** jugador, **quiero** ver el juego y la apuesta de un reto antes de unirme, **para** aceptar el mismo importe que publicó el otro jugador.

**Criterios de aceptación**

- Antes de unirse, la página muestra el juego y el importe apostado.
- Para unirse, el segundo jugador usa la misma cantidad de créditos.

### HU-08 — Descontar o devolver las apuestas según el inicio

**Como** jugador que publica o acepta un reto, **quiero** que los créditos se descuenten cuando la partida va a empezar y se devuelvan si no empieza por las causas definidas, **para** que mi saldo corresponda al estado de la partida.

**Criterios de aceptación**

- Al comenzar la partida, se descuentan las apuestas de ambos jugadores.
- Si no se une un rival, se cancela el reto o falla la página antes del inicio, se devuelven los créditos.
- La regla específica de desconexión durante una partida se describe en HU-12.

### HU-09 — Jugar moneda y probabilidad

**Como** jugador, **quiero** elegir cara o sello frente a otro jugador y ver el resultado aleatorio, **para** competir usando la probabilidad definida para la moneda.

**Criterios de aceptación**

- Cara y sello tienen cada uno 50 % de probabilidad.
- El primer jugador que elige toma el lado elegido; el otro recibe el lado restante.
- El resultado de la moneda determina quién gana.

### HU-10 — Competir en carrera de rompecabezas

**Como** jugador, **quiero** resolver un rompecabezas en mi propio tablero mientras compito con otro jugador que recibe el mismo reto, **para** ganar al terminar primero.

**Criterios de aceptación**

- Ambos jugadores reciben el mismo rompecabezas en tableros separados.
- La página muestra el tablero de cada jugador y el tablero del contrincante.
- Gana quien termine primero.
- Una desconexión o fallo técnico durante este juego implica perder los créditos apostados.

### HU-11 — Competir en palabra o acertijo

**Como** jugador, **quiero** recibir el mismo reto de palabra o acertijo que mi contrincante y enviar mi respuesta, **para** competir por ser el primero en responder correctamente.

**Criterios de aceptación**

- Los dos jugadores reciben el mismo reto y escriben su respuesta en espacios separados.
- El máximo para responder es de 35 segundos.
- Gana la primera respuesta correcta que recibe el sistema.
- Si nadie acierta, la partida continúa hasta que haya un ganador.

### HU-12 — Aplicar el resultado de la partida

**Como** jugador, **quiero** que el sistema refleje las apuestas según el resultado de la partida, **para** que el ganador reciba los créditos correspondientes.

**Criterios de aceptación**

- El ganador recibe las apuestas de ambos jugadores.
- Si un jugador se desconecta durante una partida, pierde los créditos que apostó.
- No hay empate.

### HU-13 — Continuar jugando

**Como** jugador que terminó una partida, **quiero** poder elegir si publico o acepto otro reto, **para** continuar con otra partida si así lo decido.

**Criterios de aceptación**

- El jugador puede optar por continuar.
- Para la nueva partida elige de nuevo uno de los juegos y una apuesta disponible.

## Épica 3: Comunicación y reportes

### HU-14 — Comunicarme con el otro jugador

**Como** jugador, **quiero** comunicarme con mi contrincante mediante chat de texto o de voz, **para** interactuar durante la experiencia.

**Criterios de aceptación**

- La página contempla chat de texto y chat de voz entre jugadores.
- Detalles de conexión, disponibilidad y moderación del chat están **Por definir**.

### HU-15 — Silenciar o bloquear a otro jugador

**Como** jugador, **quiero** poder silenciar o bloquear a otro jugador en el chat de texto y voz, **para** controlar esa interacción.

**Criterios de aceptación**

- La opción de silenciar está disponible para texto y voz.
- La opción de bloquear está disponible para texto y voz.
- Los efectos detallados de bloquear sobre partidas ya iniciadas están **Por definir**.

### HU-16 — Reportar a otro jugador

**Como** jugador, **quiero** reportar a otro jugador desde la interacción de texto o voz, **para** que el reporte pueda ser consultado por el administrador.

**Criterios de aceptación**

- La opción de reportar está disponible para interacciones de texto y voz.
- El reporte se envía a una base de datos.
- El administrador dispone de un apartado o vista donde consultar reportes.
- Los campos del reporte y el proceso de atención están **Por definir**.

## Épica 4: Límites y apoyos

### HU-17 — Respetar el límite diario de juego

**Como** jugador, **quiero** que la página controle mi tiempo de juego, **para** que no pueda jugar más de 90 minutos por cada periodo de 24 horas.

**Criterios de aceptación**

- El sistema contabiliza el tiempo de juego del jugador dentro de cada periodo de 24 horas.
- Al alcanzar 90 minutos, muestra un aviso de que terminó el tiempo.
- Al alcanzar el límite, impide iniciar nuevas partidas hasta el siguiente periodo.
- La zona horaria del periodo no está definida.

### HU-18 — Ver el video informativo al terminar una partida

**Como** jugador, **quiero** que al terminar una partida se me ofrezca el video informativo relacionado con los daños de los juegos de azar, **para** tener acceso a ese contenido.

**Criterios de aceptación**

- La página ofrece el video al terminar cada partida.
- No es obligatorio verlo completo.
- El video específico está **Por definir**.

### HU-19 — Tomar un descanso de cinco minutos

**Como** jugador, **quiero** tener un descanso de cinco minutos después de una partida, **para** respirar antes de continuar.

**Criterios de aceptación**

- Después de cada partida se ofrece un descanso de cinco minutos antes de continuar jugando.
- Si el jugador ve completo el video informativo, el descanso se cancela automáticamente y puede continuar sin esperar el tiempo restante.
- Si el jugador no ve el video completo, se mantiene el descanso de cinco minutos.

### HU-20 — Registrar voluntariamente un desencadenante

**Como** jugador, **quiero** registrar voluntariamente qué me ocurría cuando sentí ganas de jugar y qué hice después, **para** dejar constancia de mi experiencia.

**Criterios de aceptación**

- El registro es voluntario.
- El registro ofrece estas preguntas: “¿Qué estaba pasando justo antes de que sintieras ganas de jugar?”, “¿Qué sentías o pensabas?”, “¿Qué tan fuerte era el impulso?”, “¿Qué hiciste después?”, “¿Algo te ayudó?” y “¿Sientes que este juego te ayuda?”.
- El formato de respuesta y el lugar de la página desde donde se abre están **Por definir**.

## Decisiones todavía por definir

- Verificación del correo y recuperación de cuenta.
- Formato de respuesta y ubicación del registro voluntario.
- Contenido seleccionado para el video informativo.
- Zona horaria para contar los periodos de 24 horas.
- Detalles de implementación del chat, privacidad y retención de datos.
- Campos y proceso de atención de los reportes.
- Efectos de bloquear a alguien en una partida que ya comenzó.

