CREATE TABLE cierre_orden (
 id CHAR(36) PRIMARY KEY,
 orden_id CHAR(36) NOT NULL,
 orden_version BIGINT NOT NULL,
 confirmado BOOLEAN NOT NULL DEFAULT FALSE,
 CONSTRAINT uk_cierre_version UNIQUE (orden_id, orden_version)
);
CREATE INDEX ix_cierre_pendiente ON cierre_orden(confirmado, orden_version);
