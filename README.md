# PoliMarket

Sistema académico de gestión comercial desarrollado con un enfoque de reutilización
de software basado en componentes (**CBSE**) y el patrón de diseño **Fachada**.
Incluye una API REST, persistencia en MySQL y una plataforma web para vendedores.

## Funcionalidades

| Código | Funcionalidad | Componente |
| --- | --- | --- |
| RF-10 | Validar autorización del vendedor y estado activo del empleado | Recursos Humanos |
| RF-05 | Listar productos con existencias disponibles | Ventas y Bodega |
| RF-02 | Consultar stock para una cantidad solicitada | Bodega |
| RF-12 | Consultar proveedores asociados a un producto | Proveedores |
| RF-13 | Registrar salidas y actualizar el inventario | Entregas y Bodega |

La web también permite crear pedidos con varios productos, consultar los últimos
100 pedidos del vendedor y registrar su entrega con salida de inventario.

## Arquitectura

```text
Cliente web / consumidores REST
             |
      Controladores REST
             |
    Interfaces de las fachadas
             |
  Fachadas y servicios de negocio
             |
       Repositorios JPA
             |
           MySQL
```

Los componentes son **Ventas, Bodega, Recursos Humanos, Proveedores y Entregas**.
Las operaciones entre componentes utilizan sus fachadas públicas. Por ejemplo,
Ventas reutiliza Bodega para consultar productos y existencias, y Recursos Humanos
para validar al vendedor.

### Pedidos, entrega y salida

1. Al crear un pedido se valida al vendedor, las cantidades y el stock. Se guardan
   los precios consultados en el servidor y un contrato confirmado con número
   generado. El pedido queda `PENDIENTE`, sin reservar ni descontar existencias.
2. Al registrar la entrega se comprueba nuevamente el inventario, se descuentan
   los productos, se guardan los movimientos y el pedido queda `DESPACHADO`.
3. Todos los cambios del despacho se realizan en una misma transacción. Si falta
   stock en algún producto, se revierte la operación completa. El bloqueo de filas
   evita descontar dos veces por despachos simultáneos del mismo pedido.

La entrega registrada representa el **despacho**, no la confirmación de recepción
del cliente. Más detalles en [PEDIDOS.md](PEDIDOS.md).

## Tecnologías y requisitos

- JDK **21**, con `JAVA_HOME` apuntando al JDK.
- Spring Boot **4.1.2**, Spring MVC y Spring Data JPA / Hibernate.
- MySQL **8.0**; la conexión local se verificó con MySQL 8.0.27.
- Tomcat **11** integrado en Spring Boot.
- springdoc-openapi **3.2.0** para Swagger UI.
- HTML, CSS y JavaScript para la plataforma web.
- H2 y JUnit para pruebas automatizadas.

El proyecto incluye **Maven Wrapper**; no es necesario instalar Maven por separado.
La primera ejecución necesita acceso a Internet para descargar las dependencias.

## Ejecución local en Windows

### 1. Clonar el repositorio

```powershell
git clone https://github.com/treack/polimarket.git
cd polimarket
java -version
.\mvnw.cmd -version
```

Java y Maven deben indicar Java 21. Si acabas de instalar el JDK, reinicia la
terminal y el IDE para actualizar las variables de entorno.

### 2. Preparar MySQL

Inicia MySQL. Si tu instalación utiliza el servicio `MYSQL80`, puedes iniciarlo
desde PowerShell como administrador con `Start-Service MYSQL80`.

Ejecuta en MySQL Workbench o en el cliente MySQL, con una cuenta administradora:

```sql
CREATE DATABASE IF NOT EXISTS polimarket
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

-- Reemplaza el marcador por una contraseña propia antes de ejecutar.
CREATE USER 'polimarket_app'@'localhost'
IDENTIFIED BY 'REEMPLAZAR_POR_UNA_CLAVE_PROPIA';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
ON polimarket.* TO 'polimarket_app'@'localhost';
```

Si la cuenta ya existe, conserva su configuración y utiliza su contraseña actual;
no repitas `CREATE USER`.

### 3. Configurar las variables de entorno

En la terminal desde la que ejecutarás la aplicación:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/polimarket'
$env:DB_USERNAME = 'polimarket_app'
$clave = Read-Host 'Contraseña de MySQL para polimarket_app' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $clave).Password
```

| Variable | Valor predeterminado | Uso |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/polimarket` | Conexión JDBC |
| `DB_USERNAME` | `polimarket_app` | Usuario MySQL |
| `DB_PASSWORD` | Sin valor predeterminado; obligatoria | Contraseña MySQL |
| `DB_DDL_AUTO` | `update` | Estrategia de actualización del esquema |

Las variables del ejemplo duran mientras permanezca abierta esa terminal.
Las credenciales no deben guardarse en archivos versionados. La aplicación lee
las variables del entorno; no carga archivos `.env` automáticamente.

### 4. Iniciar la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

Hibernate crea o actualiza las tablas en la base existente mediante el modo local
`update`. Mantén la terminal abierta y comprueba el mensaje
`Started PolimarketBackendApplication`.

| Recurso | Dirección |
| --- | --- |
| Plataforma web | [http://localhost:8080/](http://localhost:8080/) |
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| OpenAPI YAML | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

Para detenerla, presiona **Ctrl + C**. Después de cambiar los recursos web,
reinicia la aplicación y actualiza el navegador con **Ctrl + F5**.

### 5. Cargar datos de prueba

Después del primer arranque, puedes ejecutar manualmente estos scripts en Workbench:

- [RF-02: producto con 20 unidades](sql/datos-prueba-rf02.sql).
- [RF-10: vendedor autorizado y vendedor no autorizado](sql/datos-prueba-rf10.sql).
- [RF-12: producto asociado a dos proveedores](sql/datos-prueba-rf12.sql).
- [RF-13: producto con stock para probar salidas](sql/datos-prueba-rf13.sql).

Los scripts muestran los identificadores creados. **Cada ejecución agrega nuevos
registros**; no se cargan automáticamente al iniciar la aplicación.

En la web, utiliza el ID de la tabla `vendedores`, no el documento del empleado.
Valida un vendedor autorizado, selecciona productos, agrega las cantidades al
pedido y completa el cliente y la dirección. Después podrás registrar su despacho.

## API REST

| Método | Ruta | Operación |
| --- | --- | --- |
| GET | `/api/rrhh/vendedores/{vendedorId}/autorizacion` | Validar vendedor |
| GET | `/api/ventas/productos` | Listar productos disponibles |
| GET | `/api/bodega/productos/{productoId}/disponibilidad?cantidad=5` | Consultar disponibilidad |
| GET | `/api/proveedores/productos/{productoId}` | Consultar proveedores |
| POST | `/api/ventas/pedidos` | Crear pedido |
| GET | `/api/ventas/pedidos?vendedorId=2` | Consultar pedidos del vendedor |
| POST | `/api/entregas/pedidos/{pedidoId}` | Registrar entrega y salida del pedido |
| POST | `/api/entregas/salidas` | Registrar una salida individual sin despachar un pedido |

Swagger permite probar las operaciones con **Try it out → Execute**. Las
operaciones de escritura modifican la base conectada. Consulta los ejemplos y
respuestas en [SWAGGER.md](SWAGGER.md) y [PEDIDOS.md](PEDIDOS.md).

## Pruebas y compilación

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Las pruebas utilizan bases temporales **H2**, sin requerir la contraseña ni
modificar la base MySQL. Cubren los requerimientos funcionales, los endpoints,
Swagger, los recursos web, la creación de pedidos, el rollback y los despachos
concurrentes. La suite actual contiene **16 pruebas**.

La compilación genera `target/polimarket-backend-0.0.1-SNAPSHOT.jar`.
Para ejecutarlo, configura las mismas variables de entorno y utiliza:

```powershell
java -jar target/polimarket-backend-0.0.1-SNAPSHOT.jar
```

## Estructura

```text
src/main/java/com/polimarket/polimarket_backend/
├── api/controller/     Controladores REST y manejo de errores
├── config/             Configuración OpenAPI
├── bodega/             Inventario y movimientos de salida
├── rrhh/               Empleados y autorización de vendedores
├── proveedores/        Proveedores y asociaciones con productos
├── ventas/             Catálogo, contratos y pedidos
└── entregas/           Entregas y coordinación del despacho
src/main/resources/
├── application.properties
└── static/             Plataforma web
src/test/               Pruebas y configuración H2
sql/                    Datos de prueba para ejecución manual
```

Los módulos organizan sus contratos, DTO, modelos, servicios y repositorios según
sus responsabilidades. La carpeta `target/` contiene archivos generados y está
excluida del repositorio.

## Alcance actual y pendientes

- La autorización de vendedor es una consulta operativa: todavía no hay inicio
  de sesión ni control de acceso autenticado. La creación y consulta de pedidos
  validan el estado del vendedor en el servidor.
- El registro de empleados, vendedores, productos y entradas de stock se realiza
  mediante SQL; aún no hay endpoints de creación para esas operaciones.
- La aplicación Android en Java está pendiente de implementar.
- La ejecución local usa HTTP y Tomcat integrado. La configuración HTTPS y el
  empaquetado WAR para un Tomcat 11 externo están pendientes.
- El modo `ddl-auto=update` corresponde al desarrollo local. El despliegue final
  requiere definir migraciones y configuración de credenciales y base de datos.

## Solución de problemas

| Mensaje | Comprobación |
| --- | --- |
| `release version 21 not supported` | Revisar `JAVA_HOME` y `.\mvnw.cmd -version`; Maven debe usar JDK 21. |
| `Access denied for user 'polimarket_app'` | Verificar usuario y contraseña y volver a definir `DB_PASSWORD` en la terminal actual. |
| `Communications link failure` | Confirmar que MySQL esté iniciado y que la URL y el puerto sean correctos. |
| Puerto 8080 ocupado | Detener la instancia anterior antes de iniciar otra. |
| No aparecen productos | Comprobar que haya registros en `productos` y `stocks` con cantidad mayor que cero. |
