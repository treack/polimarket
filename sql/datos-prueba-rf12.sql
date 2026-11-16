-- Ejecutar manualmente en Workbench. Crea un producto y dos proveedores por ejecucion.
USE polimarket;
START TRANSACTION;
INSERT INTO productos (nombre, descripcion, precio)
VALUES ('Cafe RF-12', 'Producto para consultar proveedores', 15000.00);
SET @producto_rf12 = LAST_INSERT_ID();
INSERT INTO proveedores (nombre, nit, telefono, correo)
VALUES ('Distribuidor Norte RF-12', 'PRUEBA-RF12-A', '3000000001', 'norte@example.com');
SET @proveedor_norte = LAST_INSERT_ID();
INSERT INTO proveedores (nombre, nit, telefono, correo)
VALUES ('Distribuidor Sur RF-12', 'PRUEBA-RF12-B', '3000000002', 'sur@example.com');
SET @proveedor_sur = LAST_INSERT_ID();
INSERT INTO productos_proveedores (producto_id, proveedor_id, precio_compra)
VALUES (@producto_rf12, @proveedor_norte, 12000.00),
       (@producto_rf12, @proveedor_sur, 12500.00);
COMMIT;
SELECT @producto_rf12 AS producto_id;
