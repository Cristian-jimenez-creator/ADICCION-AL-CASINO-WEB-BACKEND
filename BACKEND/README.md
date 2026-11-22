# Backend

API del **Simulador de juegos con créditos ficticios**. El proyecto aborda la necesidad de coordinar cuentas y partidas entre jugadores sin dinero real; la solución es un servicio REST que valida las reglas de juego y registra cuentas, saldos, retos y resultados en MongoDB.

El backend es un servicio separado. El frontend le envía solicitudes HTTP; solo el backend se conecta a MongoDB. La base de datos local se configura en el repositorio de [base de datos](https://github.com/Cristian-jimenez-creator/ADICCION-AL-CASINO-WEB-BASE-DE-DATOS). La presentación de diseño que acompaña el proyecto es documentación de referencia y no forma parte de este repositorio.

## Funciones

- Registro e inicio de sesión con token de acceso.
- Créditos ficticios: saldo inicial de 10.000 y recompensa de 4.000 al volver después de un ciclo de 24 horas.
- Retos públicos con apuestas de 100, 300, 800 o 2.000 créditos.
- Lógica de moneda, rompecabezas y palabra/acertijo; el banco de contenido se mantiene en `game-content/`.
- Límite de 90 minutos de juego por ciclo de 24 horas y descanso después de una partida.
- Chat, reportes, bloqueo de jugadores y señalización para voz entre jugadores. La conexión de voz entre redes necesita servidores ICE configurados en el frontend.
- Rutas REST protegidas con autorización, validación de datos y controles de seguridad.

Los créditos son ficticios: el sistema no procesa pagos ni apuestas con dinero real. Los juegos de rompecabezas y palabras dependen de que el banco de contenido esté disponible y correctamente configurado.

## Tecnologías

- Java 25 y Spring Boot 4.1.2.
- Spring Web MVC para HTTP/REST.
- Spring Data MongoDB para persistencia.
- Spring Security y validación de solicitudes.
- Maven Wrapper para ejecutar y compilar el servicio.
- Docker para el despliegue como contenedor.

## Archivos principales

- `pom.xml`: versión de Java y dependencias de Spring.
- `src/main/java/`: controladores REST y lógica de cuentas, seguridad, juegos e interacciones.
- `src/main/resources/application.properties`: valores predeterminados y lectura de variables de entorno.
- `.env.example`: nombres de variables de configuración; contiene valores de ejemplo, no credenciales reales.
- `Dockerfile`: construcción de la imagen del servicio.
- `game-content/puzzles.tsv` y `game-content/word-prompts.tsv`: contenido de los dos juegos que usan un banco de retos.

## Ejecutar localmente

Requisitos: Java 25 y una instancia MongoDB configurada como replica set. Las transacciones que mantienen consistentes las apuestas y los saldos requieren esa configuración. Para levantar MongoDB local, sigue el README del [repositorio de base de datos](https://github.com/Cristian-jimenez-creator/ADICCION-AL-CASINO-WEB-BASE-DE-DATOS).

En Windows PowerShell, desde esta carpeta:

```powershell
$env:MONGODB_URI = "mongodb://localhost:27017/simulador?replicaSet=rs0"
$env:APP_JWT_SECRET = "reemplaza-por-un-secreto-aleatorio-de-al-menos-32-bytes"
$env:APP_CORS_ALLOWED_ORIGIN = "http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

El servicio escucha en el puerto `8080` por defecto. `Spring Boot` no carga automáticamente los archivos `.env`; configura las variables en el entorno o en la plataforma de despliegue.

## Variables de entorno

| Variable | Uso |
| --- | --- |
| `MONGODB_URI` | URI de la instancia MongoDB. No la guardes en Git. |
| `APP_JWT_SECRET` | Secreto aleatorio para firmar tokens; usa al menos 32 bytes. |
| `APP_CORS_ALLOWED_ORIGIN` | Origen exacto autorizado para el frontend. |
| `APP_GAME_CONTENT_DIR` | Carpeta del banco TSV; por defecto `./game-content`. |
| `APP_ADMIN_EMAIL` | Correo de la cuenta administrativa inicial, si se configura. |
| `APP_ADMIN_PASSWORD` | Contraseña de la cuenta administrativa inicial; no se crea registro público de administradores. |

En producción define estos valores en la configuración privada del servicio (por ejemplo, Render). `MONGODB_URI` debe apuntar a Atlas, y `APP_CORS_ALLOWED_ORIGIN` debe coincidir con el dominio desplegado de Vercel. No copies credenciales de Atlas en el código, README, TXT o historial de Git.

## Contenido de los juegos

El directorio `game-content/` contiene los bancos TSV de rompecabezas y acertijos. Consulta [`game-content/README.md`](game-content/README.md) para el formato, las columnas y las reglas de carga. Las imágenes de rompecabezas deben estar disponibles en direcciones que el navegador pueda cargar.

## API

La colección Postman del proyecto enumera las rutas y ejemplos. Como referencia, las áreas principales son:

| Área | Rutas principales |
| --- | --- |
| Cuenta | `/api/auth/register`, `/api/auth/login`, `/api/auth/me`, `/api/auth/daily-reward/claim` |
| Juegos y retos | `/api/games`, `/api/challenges`, `/api/challenges/{id}` |
| Acciones de partida | `/api/challenges/{id}/coin`, `/puzzle`, `/word-answer`, `/forfeit` |
| Mensajes y voz | `/api/challenges/{id}/messages`, `/voice-signals` |
| Seguridad y reportes | `/api/players/block`, `/api/challenges/{id}/reports`, `/api/admin/reports` |

Las rutas privadas requieren `Authorization: Bearer <token>`. El catálogo de juegos y las operaciones de registro e inicio de sesión son públicas.

## Compilar y ejecutar pruebas

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

## Docker y despliegue

El `Dockerfile` de esta carpeta empaqueta el JAR y el directorio de contenido. Render puede construir desde el Dockerfile del repositorio. Configura las variables de entorno indicadas arriba y confirma que el cluster de Atlas permite conexiones desde el servicio. Atlas es el servicio de base de datos: no se despliega ni se sube un volcado de datos desde este repositorio.

