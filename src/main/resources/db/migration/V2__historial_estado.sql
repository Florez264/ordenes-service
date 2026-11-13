CREATE TABLE historial_estado (
 id CHAR(36) NOT NULL PRIMARY KEY,
 orden_id CHAR(36) NOT NULL,
 anterior VARCHAR(30),nuevo VARCHAR(30) NOT NULL,
 motivo VARCHAR(500) NOT NULL,actor VARCHAR(100) NOT NULL,
 fecha TIMESTAMP(6) NOT NULL,secuencia BIGINT NOT NULL,
 CONSTRAINT fk_historial_orden FOREIGN KEY (orden_id) REFERENCES orden_servicio(id),
 CONSTRAINT uq_historial_secuencia UNIQUE (orden_id,secuencia)
);
INSERT INTO historial_estado(id,orden_id,anterior,nuevo,motivo,actor,fecha,secuencia)
SELECT id,id,NULL,estado,'Estado observado en migración; historial anterior desconocido','MIGRACION_SIN_AUTOR_ORIGINAL',CURRENT_TIMESTAMP,version FROM orden_servicio;
