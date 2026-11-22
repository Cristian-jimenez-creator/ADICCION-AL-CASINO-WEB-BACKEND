# Backend

API REST independiente en Java 25, Spring Boot 4.1.2 y MongoDB. Este repositorio contiene solo el backend; el frontend y la configuración local de MongoDB están en repositorios separados.

## Ejecutar localmente

1. Para MongoDB local, clona el repositorio [BASE DE DATOS](https://github.com/Cristian-jimenez-creator/ADICCION-AL-CASINO-WEB-BASE-DE-DATOS) y ejecuta allí `docker compose up -d`. En producción, configura `MONGODB_URI` con tu clúster de Atlas.
2. Configura las variables del archivo `.env.example` en el entorno del proceso. Spring Boot no carga archivos `.env` automáticamente.
3. Ejecuta desde la raíz de este repositorio:

   ```powershell
   .\\mvnw.cmd spring-boot:run
   ```

La URI local predeterminada es `mongodb://localhost:27017/simulador?replicaSet=rs0`.

## Variables

- `MONGODB_URI`: conexión a MongoDB.
- `APP_JWT_SECRET`: clave para firmar sesiones; usa al menos 32 bytes aleatorios.
- `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`: crean la cuenta administrativa inicial si se proporcionan ambas.
- `APP_CORS_ALLOWED_ORIGIN`: origen autorizado para el frontend.
- `APP_GAME_CONTENT_DIR`: directorio del banco TSV; por defecto `./game-content`.

El banco de contenido está en `game-content/`. Los controladores del API se encuentran en `src/main/java/com/simulador/api/`.

## Comprobaciones e imagen Docker

```powershell
.\\mvnw.cmd test
```

El `Dockerfile` construye el backend e incluye la carpeta del banco de contenido. MongoDB se configura como servicio independiente.
