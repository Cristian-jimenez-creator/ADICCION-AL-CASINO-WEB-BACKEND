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