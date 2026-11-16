# Swagger de PoliMarket

Con MySQL iniciado y `DB_PASSWORD` configurada en la terminal, ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

- Interfaz: http://localhost:8080/swagger-ui.html
- Especificacion JSON: http://localhost:8080/v3/api-docs
- Especificacion YAML: http://localhost:8080/v3/api-docs.yaml

En Swagger UI, expandir una operacion, seleccionar **Try it out**, completar los
parametros y seleccionar **Execute**. Las solicitudes usan la API y base de datos
de la aplicacion en ejecucion. `POST /api/entregas/salidas` registra un movimiento
y descuenta inventario cada vez que tiene exito.

Los cinco requerimientos se agrupan por componente y tienen sus codigos RF en
las descripciones. RF-10 consulta el estado de autorizacion de un vendedor; no
implementa autenticacion ni protege los otros endpoints.

Tambien se incluyen la creacion y consulta de pedidos en `/api/ventas/pedidos`
y el registro de entrega con salida en `/api/entregas/pedidos/{pedidoId}`.
El flujo y ejemplos estan en [PEDIDOS.md](PEDIDOS.md).

La integracion utiliza springdoc-openapi 3.2.0, de la linea compatible con Spring
Boot 4. Las pruebas verifican la generacion del documento y los recursos de UI
con una base temporal H2, sin requerir credenciales MySQL.
