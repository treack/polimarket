package com.polimarket.polimarket_backend;

import com.polimarket.polimarket_backend.bodega.model.Producto;
import com.polimarket.polimarket_backend.bodega.model.Stock;
import com.polimarket.polimarket_backend.bodega.repository.*;
import com.polimarket.polimarket_backend.rrhh.model.Empleado;
import com.polimarket.polimarket_backend.rrhh.model.Vendedor;
import com.polimarket.polimarket_backend.rrhh.repository.*;
import com.polimarket.polimarket_backend.ventas.dto.*;
import com.polimarket.polimarket_backend.ventas.facade.IVentasFacade;
import com.polimarket.polimarket_backend.ventas.model.EstadoPedido;
import com.polimarket.polimarket_backend.ventas.repository.*;
import com.polimarket.polimarket_backend.ventas.service.OperacionPedidoException;
import com.polimarket.polimarket_backend.entregas.facade.IEntregasFacade;
import com.polimarket.polimarket_backend.entregas.repository.EntregaRepository;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:pedidos;MODE=MySQL;DB_CLOSE_DELAY=-1")
class PedidosTests {
    @Autowired ProductoRepository productos;
    @Autowired StockRepository stocks;
    @Autowired SalidaInventarioRepository salidas;
    @Autowired EmpleadoRepository empleados;
    @Autowired VendedorRepository vendedores;
    @Autowired PedidoRepository pedidos;
    @Autowired ContratoRepository contratos;
    @Autowired EntregaRepository entregas;
    @Autowired IVentasFacade ventas;
    @Autowired IEntregasFacade fachadaEntregas;
    @Autowired PlatformTransactionManager transacciones;
    @Autowired Environment environment;
    Long vendedorId;
    Long primero;
    Long segundo;

    @BeforeEach
    void preparar() {
        Empleado empleado = new Empleado();
        empleado.setActivo(true);
        empleado = empleados.saveAndFlush(empleado);
        Vendedor vendedor = new Vendedor();
        vendedor.setEmpleado(empleado);
        vendedor.setAutorizado(true);
        vendedorId = vendedores.saveAndFlush(vendedor).getId();
        primero = producto("Arroz", "2500.00");
        segundo = producto("Cafe", "5000.00");
    }

    Long producto(String nombre, String precio) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto = productos.saveAndFlush(producto);
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(20);
        stocks.saveAndFlush(stock);
        return producto.getId();
    }

    @AfterEach
    void limpiar() {
        entregas.deleteAll(); salidas.deleteAll(); pedidos.deleteAll(); contratos.deleteAll();
        stocks.deleteAll(); productos.deleteAll(); vendedores.deleteAll(); empleados.deleteAll();
    }

    CrearPedidoDto solicitud() {
        return new CrearPedidoDto(vendedorId, "Cliente prueba", "Calle 10 # 20-30",
                List.of(new LineaPedidoDto(primero, 3), new LineaPedidoDto(segundo, 2)));
    }

    HttpResponse<String> http(String ruta, Object cuerpo) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:"
                    + environment.getProperty("local.server.port") + ruta));
            if (cuerpo == null) request.GET();
            else request.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JsonMapper.builder().build().writeValueAsString(cuerpo)));
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    void creaPedidoPorHttpYDespachaUnaSolaVez() throws Exception {
        var respuesta = http("/api/ventas/pedidos", solicitud());
        assertEquals(201, respuesta.statusCode(), respuesta.body());
        var json = JsonMapper.builder().build().readTree(respuesta.body());
        Long pedidoId = json.path("id").asLong();
        assertEquals(0, json.path("total").decimalValue().compareTo(new BigDecimal("17500.00")));
        assertEquals(20, stocks.findByProductoId(primero).orElseThrow().getCantidad());
        assertEquals(0, salidas.count());
        assertEquals(EstadoPedido.PENDIENTE, ventas.listarPedidos(vendedorId).getFirst().estado());
        assertTrue(http("/api/entregas/pedidos/pendientes", null).body().contains("\"id\":" + pedidoId));
        Producto actualizado = productos.findById(primero).orElseThrow();
        actualizado.setPrecio(new BigDecimal("9999.00"));
        productos.saveAndFlush(actualizado);
        assertEquals(new BigDecimal("2500.00"), ventas.listarPedidos(vendedorId).getFirst().detalles().getFirst().precioUnitario());

        var despacho = http("/api/entregas/pedidos/" + pedidoId, java.util.Map.of());
        assertEquals(201, despacho.statusCode(), despacho.body());
        assertEquals(17, stocks.findByProductoId(primero).orElseThrow().getCantidad());
        assertEquals(18, stocks.findByProductoId(segundo).orElseThrow().getCantidad());
        assertEquals(2, salidas.count());
        assertEquals(pedidoId, salidas.findByProductoId(primero).getFirst().getPedidoId());
        assertEquals(1, entregas.count());
        assertEquals(EstadoPedido.DESPACHADO, ventas.listarPedidos(vendedorId).getFirst().estado());
        assertEquals("[]", http("/api/entregas/pedidos/pendientes", null).body());
        assertEquals(200, http("/api/ventas/pedidos?vendedorId=" + vendedorId, null).statusCode());
        assertEquals(409, http("/api/entregas/pedidos/" + pedidoId, java.util.Map.of()).statusCode());
        assertEquals(17, stocks.findByProductoId(primero).orElseThrow().getCantidad());
    }

    @Test
    void entregasConsultaPendientesDeTodosLosVendedores() throws Exception {
        var primeroCreado = ventas.crearPedido(solicitud());
        Empleado empleado = new Empleado();
        empleado.setActivo(true);
        empleado = empleados.saveAndFlush(empleado);
        Vendedor otroVendedor = new Vendedor();
        otroVendedor.setEmpleado(empleado);
        otroVendedor.setAutorizado(true);
        Long otroId = vendedores.saveAndFlush(otroVendedor).getId();
        var segundoCreado = ventas.crearPedido(new CrearPedidoDto(otroId, "Otro cliente", "Otra direccion", solicitud().detalles()));
        var respuesta = http("/api/entregas/pedidos/pendientes", null);
        assertEquals(200, respuesta.statusCode());
        var pendientes = JsonMapper.builder().build().readTree(respuesta.body());
        assertEquals(2, pendientes.size());
        assertEquals(primeroCreado.id().longValue(), pendientes.get(0).path("id").asLong());
        assertEquals(segundoCreado.id().longValue(), pendientes.get(1).path("id").asLong());
        fachadaEntregas.registrarEntrega(primeroCreado.id());
        var restantes = fachadaEntregas.listarPendientes();
        assertEquals(1, restantes.size());
        assertEquals(segundoCreado.id(), restantes.getFirst().id());
    }

    @Test
    void validaVendedorDatosCantidadesYStockAntesDeCrear() throws Exception {
        assertEquals(400, http("/api/ventas/pedidos", new CrearPedidoDto(vendedorId, "", "Direccion", solicitud().detalles())).statusCode());
        assertEquals(400, http("/api/ventas/pedidos", new CrearPedidoDto(vendedorId, "Cliente", "Direccion", List.of())).statusCode());
        assertEquals(400, http("/api/ventas/pedidos", new CrearPedidoDto(vendedorId, "Cliente", "Direccion", List.of(new LineaPedidoDto(primero, 0)))).statusCode());
        assertEquals(400, http("/api/ventas/pedidos", new CrearPedidoDto(vendedorId, "Cliente", "Direccion", List.of(new LineaPedidoDto(primero, 1), new LineaPedidoDto(primero, 2)))).statusCode());
        assertEquals(409, http("/api/ventas/pedidos", new CrearPedidoDto(vendedorId, "Cliente", "Direccion", List.of(new LineaPedidoDto(primero, 21)))).statusCode());
        assertEquals(409, http("/api/ventas/pedidos", new CrearPedidoDto(vendedorId, "Cliente", "Direccion", List.of(new LineaPedidoDto(Long.MAX_VALUE, 1)))).statusCode());
        var vendedor = vendedores.findById(vendedorId).orElseThrow();
        vendedor.setAutorizado(false); vendedores.saveAndFlush(vendedor);
        assertEquals(403, http("/api/ventas/pedidos", solicitud()).statusCode());
        assertEquals(403, http("/api/ventas/pedidos?vendedorId=" + vendedorId, null).statusCode());
        assertEquals(0, pedidos.count()); assertEquals(0, contratos.count());
        assertEquals(404, http("/api/entregas/pedidos/" + Long.MAX_VALUE, java.util.Map.of()).statusCode());
    }

    @Test
    void revierteTodasLasSalidasSiUnProductoYaNoTieneStock() {
        var pedido = ventas.crearPedido(solicitud());
        Stock stock = stocks.findByProductoId(segundo).orElseThrow();
        stock.setCantidad(1); stocks.saveAndFlush(stock);
        var error = assertThrows(OperacionPedidoException.class, () -> fachadaEntregas.registrarEntrega(pedido.id()));
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals(20, stocks.findByProductoId(primero).orElseThrow().getCantidad());
        assertEquals(1, stocks.findByProductoId(segundo).orElseThrow().getCantidad());
        assertEquals(0, entregas.count()); assertEquals(0, salidas.count());
        assertEquals(EstadoPedido.PENDIENTE, ventas.listarPedidos(vendedorId).getFirst().estado());
    }

    @Test
    void falloPosteriorRevierteEntregaStockYEstadoPedido() {
        var pedido = ventas.crearPedido(solicitud());
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transacciones).execute(status -> {
            fachadaEntregas.registrarEntrega(pedido.id());
            throw new IllegalStateException("Fallo simulado");
        }));
        assertEquals(20, stocks.findByProductoId(primero).orElseThrow().getCantidad());
        assertEquals(0, entregas.count()); assertEquals(0, salidas.count());
        assertEquals(EstadoPedido.PENDIENTE, ventas.listarPedidos(vendedorId).getFirst().estado());
    }

    @Test
    void dosDespachosSimultaneosSoloGeneranUnaEntrega() throws Exception {
        var pedido = ventas.crearPedido(solicitud());
        CountDownLatch inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> tarea = () -> {
                if (!inicio.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Tiempo agotado");
                try { fachadaEntregas.registrarEntrega(pedido.id()); return true; }
                catch (OperacionPedidoException error) { assertEquals(HttpStatus.CONFLICT, error.getStatus()); return false; }
            };
            var uno = executor.submit(tarea); var dos = executor.submit(tarea); inicio.countDown();
            assertEquals(1, (uno.get(15, TimeUnit.SECONDS) ? 1 : 0) + (dos.get(15, TimeUnit.SECONDS) ? 1 : 0));
        }
        assertEquals(1, entregas.count()); assertEquals(2, salidas.count());
        assertEquals(17, stocks.findByProductoId(primero).orElseThrow().getCantidad());
    }
}
