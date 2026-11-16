package com.polimarket.polimarket_backend;

import com.polimarket.polimarket_backend.bodega.model.Producto;
import com.polimarket.polimarket_backend.bodega.model.Stock;
import com.polimarket.polimarket_backend.bodega.repository.ProductoRepository;
import com.polimarket.polimarket_backend.bodega.repository.StockRepository;
import com.polimarket.polimarket_backend.bodega.repository.SalidaInventarioRepository;
import com.polimarket.polimarket_backend.entregas.facade.IEntregasFacade;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:rf13;MODE=MySQL;DB_CLOSE_DELAY=-1")
class SalidasTests {
    @Autowired ProductoRepository productos;
    @Autowired StockRepository stocks;
    @Autowired SalidaInventarioRepository salidas;
    @Autowired IEntregasFacade fachada;
    @Autowired PlatformTransactionManager transacciones;
    @Autowired Environment environment;
    Long productoId;
    Long stockId;

    @BeforeEach
    void preparar() {
        Producto producto = new Producto();
        producto.setNombre("Producto RF-13");
        producto = productos.saveAndFlush(producto);
        productoId = producto.getId();
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(10);
        stockId = stocks.saveAndFlush(stock).getId();
    }

    @AfterEach
    void limpiar() {
        salidas.deleteAll(salidas.findByProductoId(productoId));
        stocks.deleteById(stockId);
        productos.deleteById(productoId);
    }

    private HttpResponse<String> registrar(HttpClient client, String json) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"
                + environment.getProperty("local.server.port") + "/api/entregas/salidas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void registraPorHttpYRechazaSalidasInvalidasSinModificarStock() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            assertEquals(400, registrar(client, "{}").statusCode());
            assertEquals(400, registrar(client, "{\"cantidad\":1}").statusCode());
            assertEquals(400, registrar(client, "{\"productoId\":0,\"cantidad\":1}").statusCode());
            assertEquals(400, registrar(client, "{\"productoId\":" + productoId + ",\"cantidad\":0}").statusCode());
            assertEquals(400, registrar(client, "{\"productoId\":" + productoId + ",\"cantidad\":-1}").statusCode());
            assertEquals(400, registrar(client, "no es json").statusCode());
            assertEquals(409, registrar(client, "{\"productoId\":" + productoId + ",\"cantidad\":11}").statusCode());
            assertEquals(409, registrar(client, "{\"productoId\":" + Long.MAX_VALUE + ",\"cantidad\":1}").statusCode());
            assertEquals(10, stocks.findById(stockId).orElseThrow().getCantidad());
            assertTrue(salidas.findByProductoId(productoId).isEmpty());
            HttpResponse<String> respuesta = registrar(client, "{\"productoId\":" + productoId + ",\"cantidad\":10}");
            assertEquals(200, respuesta.statusCode());
            assertTrue(respuesta.body().contains("\"registrada\":true"));
            assertEquals(0, stocks.findById(stockId).orElseThrow().getCantidad());
            assertNotNull(stocks.findById(stockId).orElseThrow().getFechaActualizacion());
            assertEquals(1, salidas.findByProductoId(productoId).size());
            assertEquals(10, salidas.findByProductoId(productoId).getFirst().getCantidad());
        }
    }

    @Test
    void revierteMovimientoYDescuentoSiFallaLaTransaccion() {
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transacciones).execute(status -> {
            assertTrue(fachada.registrarSalidaProductos(productoId, 3));
            throw new IllegalStateException("Fallo simulado despues del registro");
        }));
        assertEquals(10, stocks.findById(stockId).orElseThrow().getCantidad());
        assertTrue(salidas.findByProductoId(productoId).isEmpty());
    }

    @Test
    void dosSolicitudesConcurrentesNoSobregiranInventario() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tarea = (java.util.concurrent.Callable<Boolean>) () -> {
                if (!inicio.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Tiempo agotado");
                return fachada.registrarSalidaProductos(productoId, 7);
            };
            var primera = executor.submit(tarea);
            var segunda = executor.submit(tarea);
            inicio.countDown();
            int exitosas = (primera.get(15, TimeUnit.SECONDS) ? 1 : 0)
                    + (segunda.get(15, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, exitosas);
        }
        assertEquals(3, stocks.findById(stockId).orElseThrow().getCantidad());
        assertEquals(1, salidas.findByProductoId(productoId).size());
    }
}
