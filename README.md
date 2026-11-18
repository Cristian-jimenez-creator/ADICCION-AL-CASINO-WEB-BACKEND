# Simulador de juegos con créditos ficticios

Aplicación web para partidas entre jugadores usando créditos ficticios. No se manejan ni representan apuestas con dinero real. Las reglas funcionales implementadas se basan en [las historias de usuario](docs/historias-de-usuario.md).

## Tecnologías

- Frontend: React 19.3 con JavaScript y Vite 7.3.8.
- Backend: Java 25 y Spring Boot 4.1.2; API REST.
- Datos: MongoDB.
- Voz: WebRTC con señalización REST.
- Pruebas backend: JUnit 5 mediante Maven Wrapper.
- Diagramas del curso: Visual Paradigm.

## Inicio local

Requisitos: Node.js, Java 25 y Docker con Docker Compose.

1. Inicia MongoDB como réplica local, necesaria para las transacciones que protegen el saldo:

   ```powershell
   docker compose -f database/compose.yaml up -d
   ```

   Espera a que el servicio `mongo-init` termine correctamente; configura el replica set con el nombre `localhost:27017` para que el backend local pueda conectarse. Hay más información en [database/README.md](database/README.md).

2. Inicia el backend en una terminal:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

3. En otra terminal, inicia la web:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

4. Abre la dirección local que muestra Vite. El backend usa `http://localhost:8080` y MongoDB local por defecto. `frontend/.env.example` y `backend/.env.example` contienen las variables de cada aplicación. La configuración de Mongo está en [database/README.md](database/README.md).

Para ejecutar las verificaciones disponibles:

```powershell
cd frontend
npm run lint
npm run build
cd ../backend
.\mvnw.cmd test
```

## Cuentas

- El registro de jugadores solicita nombre, Gmail, contraseña y cómo conoció el juego. Cada cuenta nueva recibe 10.000 créditos ficticios.
- El jugador vuelve a entrar con Gmail y contraseña.
- Una cuenta administrativa se crea al configurar `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` en el entorno del backend. La contraseña administrativa debe tener al menos 12 caracteres. No hay registro público de administradores.
- Desde el segundo periodo de 24 horas se agregan 4.000 créditos ficticios por periodo.

## Partidas

- Los juegos son moneda y probabilidad, carrera de rompecabezas y palabra/acertijo.
- Las apuestas permitidas son 300, 800 y 2.000 créditos. La persona que se une iguala la apuesta publicada.
- El saldo se descuenta al iniciar la partida; el ganador recibe las dos apuestas.
- Cada partida de palabra/acertijo tiene un reto de 35 segundos; si nadie acierta, el backend entrega el siguiente.
- El máximo diario es 90 minutos de partidas activas por cada periodo de 24 horas. Alcanzar el límite bloquea nuevas partidas y deja terminar una ya empezada.
- Al terminar una partida se presenta el espacio del video y el descanso de cinco minutos. Completar el video elimina el tiempo restante de descanso.

## Contenido que debe proporcionarse

Los juegos de rompecabezas y palabras aparecen como no disponibles hasta que se carguen sus contenidos. El formato se explica en [backend/game-content/README.md](backend/game-content/README.md). No se incluyeron acertijos, respuestas ni imágenes inventadas.

La URL del video aún está pendiente. `VITE_INFORMATIVE_VIDEO_URL` debe apuntar a un archivo de video que el navegador pueda reproducir para que el evento de finalización cancele la pausa.

Para probar voz entre redes se necesita configurar `VITE_WEBRTC_ICE_SERVERS` con los servidores ICE/STUN/TURN elegidos. Está vacío por defecto; el proyecto no selecciona un proveedor externo.

## API

Las rutas se describen en la colección [Postman](postman/simulador-api.postman_collection.json). Todas requieren `Authorization: Bearer <token>` excepto registro, inicio de sesión y catálogo de juegos.

| Área | Rutas principales |
|---|---|
| Cuenta | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/rest/complete` |
| Catálogo | `GET /api/games` |
| Retos | `GET /api/challenges/waiting`, `POST /api/challenges`, `POST /api/challenges/{id}/join`, `GET /api/challenges/{id}` |
| Partida | `POST /api/challenges/{id}/coin`, `PUT /api/challenges/{id}/puzzle`, `POST /api/challenges/{id}/word-answer`, `POST /api/challenges/{id}/forfeit` |
| Interacción | `/api/challenges/{id}/messages`, `/reports`, `/voice-signals`; `POST /api/players/block` |
| Registro voluntario | `POST /api/trigger-logs`, `GET /api/trigger-logs` |
| Administración | `GET /api/admin/reports` |

## Despliegue

El frontend, backend y servicio de base de datos están en carpetas separadas. La carpeta `frontend` está configurada como proyecto Vercel estático (`vercel.json`). El backend incluye un `Dockerfile` para desplegarlo como servicio de contenedor; se deben configurar las variables de entorno, MongoDB en modo replica set y `VITE_API_URL` / CORS con las direcciones reales. El despliegue no se ha ejecutado desde este repositorio.

## Límites pendientes

- Banco y formato definitivo de rompecabezas/acertijos.
- URL compatible del video informativo.
- Configuración ICE/STUN/TURN de voz.
- Desconexión: salir de la partida y cerrar la pestaña intentan registrar la derrota. Una pérdida de red sin evento de cierre no puede detectarse automáticamente hasta definir un intervalo de desconexión.
- Proveedor y valores del entorno de despliegue.

Consulta [el plan de implementación](docs/plan-implementacion.md) para la trazabilidad y decisiones técnicas.
