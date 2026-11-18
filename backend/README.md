# Backend

API REST en Java 25 con Spring Boot 4.1.2 y MongoDB. Esta carpeta se puede construir o desplegar como servicio por separado del frontend y de MongoDB.

## Ejecutar localmente

1. Inicia MongoDB siguiendo [`../database/README.md`](../database/README.md).
2. Copia `.env.example` a `.env` como referencia y configura las variables en el entorno del proceso. Spring Boot no carga archivos `.env` automáticamente.
3. Desde esta carpeta ejecuta:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

La URI local predeterminada es `mongodb://localhost:27017/simulador?replicaSet=rs0`. En producción, configura `MONGODB_URI` con una instancia MongoDB compatible con transacciones.

## Variables

- `MONGODB_URI`: conexión a MongoDB.
- `APP_JWT_SECRET`: clave para firmar sesiones; usa al menos 32 bytes aleatorios.
- `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`: crean la cuenta administrativa inicial si se proporcionan ambas.
- `APP_CORS_ALLOWED_ORIGIN`: origen autorizado para el frontend.
- `APP_GAME_CONTENT_DIR`: directorio del banco TSV; por defecto `./game-content` relativo a esta carpeta.

El formato del banco está en [`game-content/README.md`](game-content/README.md). La interfaz del API y sus rutas se detallan en el README de la raíz del proyecto.

## Pruebas

```powershell
.\mvnw.cmd test
```

## Imagen Docker

El `Dockerfile` construye el backend e incluye la carpeta del banco de contenido. Se configura por variables de entorno; MongoDB se ejecuta como servicio independiente.
