CREATE TABLE orden_servicio (
 id CHAR(36) NOT NULL PRIMARY KEY,
 cliente_nombre VARCHAR(150) NOT NULL,
 cliente_documento VARCHAR(30) NOT NULL,
 cliente_telefono VARCHAR(25) NOT NULL,
 placa VARCHAR(10) NOT NULL,
 marca VARCHAR(60) NOT NULL,
 modelo VARCHAR(60) NOT NULL,
 aseguradora_codigo VARCHAR(40),
 aseguradora_nombre VARCHAR(150),
 descripcion VARCHAR(2000) NOT NULL,
 estado VARCHAR(30) NOT NULL,
 fecha_creacion TIMESTAMP(6) NOT NULL,
 version BIGINT NOT NULL
);
CREATE INDEX idx_orden_fecha ON orden_servicio(fecha_creacion,id);
CREATE INDEX idx_orden_estado_fecha ON orden_servicio(estado,fecha_creacion);
CREATE INDEX idx_orden_placa ON orden_servicio(placa);
