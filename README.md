# Backend del simulador

API REST para el simulador de juegos con créditos ficticios. El problema que resuelve es coordinar cuentas, retos y partidas entre personas; el backend aplica las reglas, protege las operaciones del saldo y persiste la información en MongoDB.

## Tecnologías

- Java 25 y Spring Boot 4.1.2.
- Spring Web MVC, Spring Security, validación y Spring Data MongoDB.
- Maven Wrapper y Docker.

## Cómo se conecta

```text
Frontend en Vercel → API REST en Render → MongoDB Atlas
```

El frontend nunca se conecta directamente a MongoDB. La URI de Atlas se configura como secreto `MONGODB_URI` en Render; las credenciales no deben guardarse en GitHub.

## Funciones principales

- Registro, inicio de sesión y autenticación por token.
- Cuentas con créditos ficticios, recompensas periódicas y límite de tiempo de juego.
- Retos para moneda, rompecabezas y palabra/acertijo.
- Chat, reportes, bloqueo y señalización para voz entre jugadores.
- Contenido de rompecabezas y palabras en archivos TSV.

No maneja dinero real ni procesa pagos.

## Archivos

- `BACKEND/src/main/java/`: API y reglas de negocio.
- `BACKEND/src/main/resources/application.properties`: configuración y variables de entorno.
- `BACKEND/pom.xml`: Java y dependencias.
- `BACKEND/Dockerfile`: imagen de despliegue.
- `BACKEND/game-content/`: bancos de contenido.
- `BACKEND/README.md`: instrucciones completas de ejecución, configuración y API.

Para ejecutar localmente necesitas Java 25 y MongoDB como replica set. Consulta la [guía completa](BACKEND/README.md) y la [guía de contenido](BACKEND/game-content/README.md).

## Reglas confirmadas en el código

- Una cuenta nueva recibe **10.000 créditos ficticios**. Al reclamar la recompensa después de un nuevo ciclo de 24 horas se agregan **4.000**; los ciclos omitidos no se acumulan.
- La actividad diaria tiene un máximo de **90 minutos**. El servicio limita el tiempo de actividad fuera de una partida usando pulsos de actividad.
- Las apuestas disponibles son **100, 300, 800 y 2.000**. Al iniciar un reto, se descuenta la apuesta de ambos jugadores; quien gana recibe el pozo equivalente a dos apuestas.
- La ronda de palabra tiene **40 segundos**. La pista de la ronda se comparte con ambos jugadores.
- Al terminar una partida se aplica un descanso de **5 minutos**.

## Rutas de referencia

| Acción | Método y ruta |
| --- | --- |
| Crear cuenta / iniciar sesión | POST /api/auth/register / POST /api/auth/login |
| Consultar sesión / reclamar recompensa | GET /api/auth/me / POST /api/auth/daily-reward/claim |
| Registrar actividad | POST /api/auth/activity/pulse |
| Listar retos / publicar reto | GET /api/challenges/waiting / POST /api/challenges |
| Unirse o consultar reto | POST /api/challenges/{id}/join / GET /api/challenges/{id} |
| Jugar | POST /api/challenges/{id}/coin, PUT /api/challenges/{id}/puzzle, POST /api/challenges/{id}/word-answer |
| Enviar pista / abandonar | POST /api/challenges/{id}/word-hint / POST /api/challenges/{id}/forfeit |

Las operaciones de cuenta requieren token Bearer cuando son privadas. Para rutas de interacción adicionales (chat, reportes, bloqueo y voz), revisa los controladores en BACKEND/src/main/java/ y la colección Postman del repositorio.

## Configuración de contenido

Los archivos game-content/puzzles.tsv y game-content/word-prompts.tsv alimentan rompecabezas y palabras. Si falta contenido válido, el backend no puede ofrecer esos minijuegos. La ruta puede cambiarse con APP_GAME_CONTENT_DIR; confirma que esa carpeta se incluya en la imagen y esté disponible desde el directorio de trabajo del contenedor.

## Comprobaciones de despliegue

Si el servicio compila pero falla al iniciar, revisa en este orden: MONGODB_URI (Atlas, no localhost), acceso de red del cluster a Render, APP_JWT_SECRET, origen exacto en APP_CORS_ALLOWED_ORIGIN y disponibilidad de game-content/. No pegues valores secretos en incidencias ni registros compartidos.
