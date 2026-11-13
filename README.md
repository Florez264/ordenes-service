# ordenes-service

Gestion de ordenes, estados e historial de auditoria. No contiene credenciales ni logica de WhatsApp.

Repositorio independiente. Java 21, Spring Boot, Maven y MySQL. Arquitectura DDD por puertos y adaptadores: dominio, aplicacion e infraestructura. Base propia: `ordenes_db`; no accede a las tablas del otro servicio.

## Desarrollo

Desde esta carpeta, copiar `.env.local.example` a `.env.local`, ajustar valores y ejecutar `mvnw.cmd spring-boot:run` en PowerShell (Linux: `./mvnw spring-boot:run`). Se necesita MySQL preparado; el repositorio hermano `subocol-infra` incluye la inicializacion y puede arrancarlo con `docker compose up -d mysql`. Puerto del servicio: 8081. Pruebas: `mvnw.cmd verify`.

## Imagen y nube

Construir desde esta carpeta: `docker build -t subocol-ordenes:local .`. La imagen contiene solo este servicio. Usar `.env.cloud.example` como referencia para las variables del proveedor, con `SPRING_PROFILES_ACTIVE=cloud`; nunca subir secretos ni archivos `.env.local` reales. MySQL y los usuarios de aplicacion/migracion deben existir previamente. La nube no importa archivos dotenv locales.

CORS: local permite `http://localhost:4200`; en nube definir `CORS_ALLOWED_ORIGINS` con el dominio real de Angular, sin barra final. Se pueden separar varios con coma. Basic Auth se conserva y en nube requiere HTTPS.

Agenda se comunica con Ordenes por `ORDENES_URL`; las credenciales `INTERNAL_USER` y `INTERNAL_PASSWORD` deben coincidir entre ambos despliegues. La salud publica esta en `/actuator/health`. Las respuestas de negocio siguen `{status,data}`.

El cierre de una orden se sincroniza con Agenda mediante un buzon persistente: cancela las reservas anteriores al cierre, libera cupos y no encola WhatsApp de cancelacion. La sincronizacion es eventual (intervalo por defecto: 15 segundos, mas tiempo si hay una caida). La integracion completa, coleccion Postman y decisiones comunes estan en el repositorio `subocol-infra`.

## Integracion continua y Railway

Ver [DESPLIEGUE.md](DESPLIEGUE.md). CI verifica el Dockerfile y publica una imagen independiente en GHCR. Las variables de nube estan en `.env.cloud.example`; los secretos reales se cargan en Railway.


## Cierre de orden y cancelación automática

Órdenes guarda un evento en `cierre_orden` en la misma transacción que estado e historial. Agenda consulta hasta 20 pendientes cada 15 segundos por la red interna, aplica la cancelación y confirma el evento después del commit. Si falla la red o la confirmación, reintenta; la operación es idempotente. No se comparte ninguna tabla entre servicios ni se agrega otro contenedor.

El evento lleva la versión del cierre. Cada reserva conserva la versión de la orden consultada y usa un bloqueo por orden antes del bloqueo de franja. Esto evita perder reservas concurrentes al procesar un cierre, rechaza respuestas obsoletas y permite reservas nuevas tras una reapertura. La preparación del bloqueo se confirma antes de entrar en la transacción de reserva para no agotar el pool reducido. Se cancelan reservas anteriores al cierre, incluidas las que hayan quedado pendientes en horarios pasados. La reapertura no reactiva citas canceladas.

La cancelación automática registra motivo y actor `sistema-cierre`, libera capacidad y NO crea `CANCELADA_MANUAL`. Los avisos anteriores de agendamiento no se eliminan ni los mensajes ya aceptados se retractan.

Desplegar primero Órdenes (migración V4 y nuevo contrato interno con versión) y después Agenda (migración V3). Se usan las credenciales internas existentes; no hacen falta secretos nuevos. Variables opcionales en Agenda: `ORDER_CLOSURES_WORKER_ENABLED=true`, `ORDER_CLOSURES_POLL_DELAY_MS=15000` y `ORDER_CLOSURES_INITIAL_DELAY_MS=10000`.

Verificación: 22 pruebas en Órdenes y 32 en Agenda, incluidas atomicidad y rollback del buzón, seguridad del acuse, idempotencia, cupos, ausencia de aviso de cancelación y reapertura. Prueba integrada HTTP local con dos procesos y bases H2 independientes: cierre, consumo y confirmación del evento, cupo recuperado y reserva posterior a reapertura preservada. H2 no sustituye la validación de migraciones en MySQL al desplegar.
