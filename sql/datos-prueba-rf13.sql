-- Ejecutar manualmente en Workbench. Crea un producto nuevo por ejecucion.
USE polimarket;
START TRANSACTION;
INSERT INTO productos (nombre, descripcion, precio)
VALUES ('Producto RF-13', 'Prueba de salidas de inventario', 5000.00);
SET @producto_rf13 = LAST_INSERT_ID();
INSERT INTO stocks (producto_id, cantidad, fecha_actualizacion, version)
VALUES (@producto_rf13, 20, CURRENT_TIMESTAMP, 0);
COMMIT;
SELECT @producto_rf13 AS producto_id, 20 AS existencias;
-- Tras invocar el endpoint, consultar:
-- SELECT * FROM stocks WHERE producto_id = @producto_rf13;
-- SELECT * FROM salidas_inventario WHERE producto_id = @producto_rf13;
