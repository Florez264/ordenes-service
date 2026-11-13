# CI, imagen Docker y Railway

El workflow `.github/workflows/ci.yml` construye en Linux con el Dockerfile. `mvn verify` se ejecuta en la etapa de build y debe pasar antes de generar/publicar la imagen. Pull requests verifican sin publicar. Push a la rama predeterminada publica en GHCR `latest` y `sha-<commit completo>`; no requiere crear tokens personales de GitHub ni compartir credenciales de Railway. Se utiliza GITHUB_TOKEN con packages:write. El cache de compilacion reduce trabajo entre ejecuciones. Las pruebas actuales usan H2; no necesitan las credenciales de Clever Cloud ni Meta.

## Primera publicacion

1. Subir estos cambios a la rama predeterminada y comprobar Actions en verde.
2. En GitHub, perfil > Packages > paquete de este servicio: configurar visibilidad publica si se permite publicar la imagen. Los paquetes nuevos pueden ser privados aunque el repositorio sea publico. Para imagen privada usar las credenciales de registro que acepte Railway, sin ponerlas en el workflow ni en Git.
3. En Railway crear un proyecto Subocol y un servicio desde Docker Image, no otro servicio adicional desde el mismo repositorio. Usar `ghcr.io/florez264/ordenes-service:latest` (sustituir ordenes-service por el nombre de este repositorio). Los dos backends deben estar en el mismo proyecto, entorno y preferiblemente region.
4. Antes de desplegar, cargar las variables de `.env.cloud.example`, sustituyendo TODOS los marcadores y campos obligatorios vacios. Los valores reales se configuran en Railway; la imagen no contiene dotenv ni credenciales. Mantener `SPRING_PROFILES_ACTIVE=cloud`.
5. Configurar Healthcheck Path `/actuator/health`, timeout 120 segundos, una replica y reinicio ON_FAILURE con maximo 3 intentos. Usar el ENTRYPOINT de la imagen; no hace falta comando start/build personalizado. Establecer PORT segun el ejemplo del servicio.
6. Comenzar con limite de memoria de 512 MB por servicio si el plan lo permite. Es un limite de seguridad, no una reserva ni una promesa de consumo. Observar RAM/CPU y errores antes de reducirlo; los 192 MB de heap no equivalen al total de memoria de Java. No asignar volumen a las APIs: la persistencia esta en MySQL.
7. Generar dominio publico HTTPS para los endpoints usados desde Angular o Postman. En Agenda, ORDENES_URL apunta al dominio PRIVADO de Ordenes con su puerto 8081. Si renombras el servicio Railway, ajustar ese dominio. INTERNAL_USER/PASSWORD deben coincidir. CORS_ALLOWED_ORIGINS debe ser el origen real de Angular.
8. Desplegar primero Ordenes y comprobar salud; despues Agenda. Crear orden/cita y revisar notificaciones. Empezar en LOG; activar WHATSAPP con variables privadas de Meta solo al probar el envio real.

## Clever Cloud

Se requiere una base independiente por microservicio y no modificar la base de Franquicia. Completar DB_URL (JDBC, no R2DBC), DB_USER, DB_PASSWORD y las credenciales de migracion. Si el proveedor entrega un unico usuario para esa base, se puede usar como usuario de aplicacion y migracion, documentando que tiene permisos mas amplios; es preferible separar ambos si el plan lo permite. No usar el usuario de Ordenes en la base de Agenda ni al contrario.

Confirmar TLS con el proveedor; la plantilla usa sslMode=VERIFY_IDENTITY y requiere certificado/nombre valido. No desactivar la verificacion para ocultar problemas de certificados. Flyway necesita permisos DDL y, en Ordenes, CREATE TRIGGER. Si el proveedor impide crear triggers, hay que resolver permisos antes de marcar el despliegue listo; no eliminar esa proteccion de auditoria. No se crea infraestructura ni se generan cargos por ejecutar CI.

## Actualizaciones y ahorro

Una imagen nueva en GHCR no garantiza por si sola un despliegue en Railway. Activar Image Auto Updates y su ventana de mantenimiento para `latest`, o actualizar manualmente a `sha-<commit>` y desplegar. Para rollback seleccionar una etiqueta SHA anterior. No se ha configurado acceso automatico a la cuenta de Railway desde este repositorio.

Las imagenes contienen solo JRE y el JAR; Maven y las pruebas quedan en la etapa de build. Java usa heap inicial 32 MB, maximo 192 MB, memoria directa maxima 32 MB y SerialGC, ajustables con JAVA_TOOL_OPTIONS. No hay un limite artificial pequeno de metaspace que provoque reinicios por carga de clases. El perfil cloud usa pool JDBC de 2 conexiones, minimo 0 y hasta 16 hilos HTTP. El lote de notificaciones sigue limitado y Agenda consulta cada 60 segundos en cloud: puede demorar ese tiempo en iniciar un envio. Mantener worker-enabled=true para procesar y reintentar avisos.

La consulta periodica de Agenda genera trafico a MySQL y puede impedir el modo Serverless. No activar la suspension esperando ahorro garantizado ni deshabilitar el worker porque dejaria avisos pendientes sin procesar. Usar una replica por servicio, revisar metricas y configurar limite de gasto y alertas en Railway. Un limite de gasto puede detener servicios. No se puede garantizar cuanto duran los creditos sin saldo actual y consumo medido. Detener los servicios cuando ya no se necesite la demostracion es la opcion mas directa para evitar consumo de ejecucion.

## Referencias

- [Publicacion de imagenes con GitHub Actions](https://docs.github.com/en/actions/tutorials/publish-packages/publish-docker-images)
- [Railway: servicios e imagenes](https://docs.railway.com/services)
- [Railway: actualizaciones de imagenes](https://docs.railway.com/deployments/image-auto-updates)
- [Railway: red privada](https://docs.railway.com/networking/private-networking)
- [Railway: controles de gasto](https://docs.railway.com/pricing/cost-control)
- [Railway: Serverless y trafico saliente](https://docs.railway.com/deployments/serverless)

## Verificacion local de esta configuracion

2026-11-12: imagen linux/amd64 construida correctamente; 20 pruebas pasaron dentro de Docker. Contenedor no root con perfil cloud, heap 192 MB, limite 512 MiB y 1 CPU: salud UP y listado autenticado HTTP 200 contra MySQL local. Memoria observada tras arranque y una consulta: aproximadamente 295.1 MiB. Es una muestra breve, no una prueba de carga ni garantia de RAM/costo en Railway. No se uso Meta real; el worker se desactivo solo durante este chequeo de arranque para no consumir avisos existentes. Los contenedores temporales se retiraron y los datos se conservaron.
