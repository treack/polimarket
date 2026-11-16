# Pedidos, entrega y salida de inventario

La plataforma web en `/` permite validar el vendedor, agregar uno o varios
productos al pedido y completar el nombre del cliente y la direccion.

## Flujo

1. Crear el pedido: valida la autorizacion del vendedor, cantidades y stock.
   Guarda el precio de cada producto obtenido del servidor y un contrato
   confirmado con numero generado. El pedido queda `PENDIENTE` y no reserva
   ni descuenta existencias.
2. Registrar entrega y salida: bloquea el pedido, vuelve a comprobar el stock
   y registra un movimiento por producto en `salidas_inventario`. Cada movimiento
   conserva `pedido_id`. Guarda una entrega y cambia el pedido a `DESPACHADO`.
3. La entrega, todos los movimientos, el stock y el estado del pedido se
   confirman en una misma transaccion. Si falla un producto se revierte todo.
   Un segundo despacho del mismo pedido se rechaza con HTTP 409.

La fecha de entrega registrada corresponde a la salida o despacho; este flujo
no confirma que el cliente haya recibido los productos. Las salidas individuales
de RF-13 siguen disponibles y no despachan pedidos.

## API y Swagger

- `POST /api/ventas/pedidos`: crea un pedido (HTTP 201).
- `GET /api/ventas/pedidos?vendedorId=2`: ultimos 100 pedidos del vendedor.
- `POST /api/entregas/pedidos/1`: registra entrega y salida (HTTP 201), sin cuerpo.

Ejemplo de creacion (usar IDs existentes):

```json
{
  "vendedorId": 2,
  "cliente": "Cliente de prueba",
  "direccion": "Calle 10 # 20-30",
  "detalles": [
    {"productoId": 1, "cantidad": 3}
  ]
}
```

No enviar precios: se consultan en Bodega. Los productos no pueden repetirse
en los detalles; cada linea tiene una cantidad positiva. Se admiten hasta
100 lineas. Errores: 400 datos invalidos, 403 vendedor no autorizado,
404 pedido inexistente y 409 stock insuficiente o pedido ya despachado.

Swagger permanece en `/swagger-ui.html`. Al reiniciar con el modo local
`ddl-auto=update`, Hibernate agrega las columnas de pedido y vinculo de salida,
y la restriccion de una entrega por pedido. No se requiere borrar las tablas.
Una base que ya tenga entregas duplicadas por pedido debe reconciliar esos
registros antes de aplicar la restriccion; no se eliminan automaticamente.

Las consultas de autorizacion siguen siendo operativas; no constituyen inicio
de sesion. El listado y la creacion comprueban la autorizacion del vendedor
en el servidor, pero la aplicacion aun no autentica usuarios.

## Verificacion

`./mvnw.cmd test` usa H2 temporal. Las pruebas cubren creacion por HTTP,
validacion, precio guardado, ausencia de descuento al crear, registro de
entrega, reversibilidad, falta de stock en un pedido multiproducto y despachos
simultaneos. La base MySQL del usuario no se modifica durante esas pruebas.
