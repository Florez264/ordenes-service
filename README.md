# ordenes-service

Gestion de ordenes, estados e historial de auditoria. No contiene credenciales ni logica de WhatsApp.

Repositorio independiente. Java 21, Spring Boot, Maven y MySQL. Arquitectura DDD por puertos y adaptadores: dominio, aplicacion e infraestructura. Base propia: `ordenes_db`; no accede a las tablas del otro servicio.

## Desarrollo

Desde esta carpeta, copiar `.env.local.example` a `.env.local`, ajustar valores y ejecutar `mvnw.cmd spring-boot:run` en PowerShell (Linux: `./mvnw spring-boot:run`). Se necesita MySQL preparado; el repositorio hermano `subocol-infra` incluye la inicializacion y puede arrancarlo con `docker compose up -d mysql`. Puerto del servicio: 8081. Pruebas: `mvnw.cmd verify`.

## Imagen y nube

Construir desde esta carpeta: `docker build -t subocol-ordenes:local .`. La imagen contiene solo este servicio. Usar `.env.cloud.example` como referencia para las variables del proveedor, con `SPRING_PROFILES_ACTIVE=cloud`; nunca subir secretos ni archivos `.env.local` reales. MySQL y los usuarios de aplicacion/migracion deben existir previamente. La nube no importa archivos dotenv locales.

CORS: local permite `http://localhost:4200`; en nube definir `CORS_ALLOWED_ORIGINS` con el dominio real de Angular, sin barra final. Se pueden separar varios con coma. Basic Auth se conserva y en nube requiere HTTPS.

Agenda se comunica con Ordenes por `ORDENES_URL`; las credenciales `INTERNAL_USER` y `INTERNAL_PASSWORD` deben coincidir entre ambos despliegues. La salud publica esta en `/actuator/health`. Las respuestas de negocio siguen `{status,data}`.

La cancelacion automatica de citas al cerrar una orden sigue pendiente. La integracion completa, coleccion Postman y decisiones comunes estan en el repositorio `subocol-infra`.

## Integracion continua y Railway

Ver [DESPLIEGUE.md](DESPLIEGUE.md). CI verifica el Dockerfile y publica una imagen independiente en GHCR. Las variables de nube estan en `.env.cloud.example`; los secretos reales se cargan en Railway.
