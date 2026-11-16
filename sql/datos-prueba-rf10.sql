-- Ejecutar manualmente en Workbench. Crea dos vendedores nuevos por ejecucion.
USE polimarket;
START TRANSACTION;
INSERT INTO empleados (nombre, documento, activo)
VALUES ('Vendedor autorizado RF-10', 'PRUEBA-RF10-A', TRUE);
SET @empleado_autorizado = LAST_INSERT_ID();
INSERT INTO vendedores (empleado_id, autorizado) VALUES (@empleado_autorizado, TRUE);
SET @vendedor_autorizado = LAST_INSERT_ID();
INSERT INTO empleados (nombre, documento, activo)
VALUES ('Vendedor no autorizado RF-10', 'PRUEBA-RF10-B', TRUE);
SET @empleado_no_autorizado = LAST_INSERT_ID();
INSERT INTO vendedores (empleado_id, autorizado) VALUES (@empleado_no_autorizado, FALSE);
SET @vendedor_no_autorizado = LAST_INSERT_ID();
COMMIT;
SELECT @vendedor_autorizado AS vendedor_autorizado_id,
       @vendedor_no_autorizado AS vendedor_no_autorizado_id;
