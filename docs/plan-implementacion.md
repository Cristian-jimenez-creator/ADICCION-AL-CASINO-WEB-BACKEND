# Implementación y trazabilidad

## Fuente de alcance

El comportamiento funcional se basa en las historias confirmadas en [historias-de-usuario.md](historias-de-usuario.md). Los créditos son ficticios y el sistema no recibe dinero. Las historias pendientes de definición se mantienen como límites de implementación.

## Tecnologías

- React 19.3 y JavaScript, compilados con Vite 7.3.8.
- Java 25 y Spring Boot 4.1.2; API REST.
- MongoDB con transacciones y persistencia de cuentas, retos y reportes.
- WebRTC para voz; señalización mediante rutas REST.
- JUnit 5 / Maven Wrapper 3.10.1, Postman y Vercel forman parte de los materiales del proyecto.
- Visual Paradigm sigue siendo la herramienta indicada para los diagramas.

## Implementación por historia

| Historias | Estado implementado |
|---|---|
| HU-01–04 | Registro con los cuatro campos, inicio de sesión, 10.000 créditos iniciales, 4.000 por periodo desde el segundo día, saldo acumulado. |
| HU-05–08 | Catálogo de los tres juegos, apuestas de 300/800/2.000, publicación y unión a retos con apuesta igual; descuento al comenzar. El reto de espera no descuenta créditos. |
| HU-09 | Selección de cara/sello y resultado aleatorio equiprobable, con pago de las dos apuestas al ganador. |
| HU-10 | Dos tableros separados a partir del mismo contenido configurado; gana el primero que envía la solución. |
| HU-11 | El mismo enunciado para ambos jugadores, 35 segundos por reto y siguiente reto hasta que alguien responda correctamente. |
| HU-12–13 | Resultado y saldo; abandonar por el botón o evento de cierre del navegador intenta resolver la partida como derrota; publicar o aceptar otro reto desde la sala. |
| HU-14–16 | Chat de texto, habilitación de voz WebRTC, silenciar, bloquear, reportar y consulta administrativa protegida. |
| HU-17 | Tiempo activo medido desde el inicio de la partida, máximo 90 minutos por ciclo de 24 horas y bloqueo de nuevas partidas al alcanzarlo. La partida en curso puede terminar. |
| HU-18–19 | Espacio para video y descanso de cinco minutos; el descanso queda registrado en la cuenta y bloquea nuevos retos hasta que termine o se informe la reproducción completa del video. |
| HU-20 | Formulario voluntario con las seis preguntas, persistido por cuenta y consultable por esa cuenta. |

## Datos y servicios pendientes

- La persona solicitante entregará el banco de rompecabezas/acertijos y la URL del video. El formato de datos aceptado está en [game-content/README.md](../game-content/README.md). No se han creado preguntas, respuestas ni imágenes de ejemplo.
- `VITE_INFORMATIVE_VIDEO_URL` requiere una URL de archivo reproducible por el elemento HTML `<video>` para que el evento `ended` cancele el descanso.
- La voz requiere valores `VITE_WEBRTC_ICE_SERVERS` apropiados para conectar jugadores a través de distintas redes. No se eligió proveedor.
- El registro voluntario queda guardado en MongoDB. Periodo de retención y borrado de estos datos no están definidos.
- Cerrar la pestaña envía una solicitud de derrota como mejor esfuerzo; una pérdida de red sin evento de cierre requiere definir un tiempo de desconexión antes de detectarse automáticamente.
- Verificación del Gmail, recuperación de contraseña y tratamiento del bloqueo cuando una partida ya empezó no están definidos.
- Dirección de API, MongoDB, credenciales administrativas, CORS y variables de despliegue se deben cargar en el entorno objetivo. No se ejecutó despliegue.

## Persistencia y saldo

El inicio de partida y la liquidación del saldo usan transacciones MongoDB. Por eso la base local se inicia como replica set con `docker compose`; el MongoDB desplegado también debe aceptar transacciones. La cuenta se actualiza con el tiempo activo y los ciclos diarios definidos en la historia.

## Verificación realizada

- Frontend: `npm run lint` y `npm run build`.
- Backend: `mvnw test`, con pruebas unitarias de ciclos diarios, conteo de tiempo activo y transiciones del reto.
- Los flujos con MongoDB real, WebRTC entre redes, contenido del usuario y despliegue requieren su configuración correspondiente y siguen pendientes de verificación en esos entornos.
