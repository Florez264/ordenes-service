CREATE TRIGGER historial_sin_update BEFORE UPDATE ON historial_estado
FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial es de solo anexado';
CREATE TRIGGER historial_sin_delete BEFORE DELETE ON historial_estado
FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial es de solo anexado';
