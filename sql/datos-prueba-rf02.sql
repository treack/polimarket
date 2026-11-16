-- Ejecutar manualmente en Workbench despues de arrancar la aplicacion.
-- Crea un producto nuevo en cada ejecucion y muestra su identificador.
USE polimarket;
START TRANSACTION;
INSERT INTO productos (nombre, descripcion, precio)
VALUES ('Arroz RF-02', 'Producto para probar disponibilidad', 4500.00);
SET @producto_prueba = LAST_INSERT_ID();
INSERT INTO stocks (producto_id, cantidad, fecha_actualizacion, version)
VALUES (@producto_prueba, 20, CURRENT_TIMESTAMP, 0);
COMMIT;
SELECT @producto_prueba AS producto_id, 20 AS existencias;
