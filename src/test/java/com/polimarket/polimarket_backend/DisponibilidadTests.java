package com.polimarket.polimarket_backend;

import com.polimarket.polimarket_backend.bodega.facade.IBodegaFacade;
import com.polimarket.polimarket_backend.bodega.model.Producto;
import com.polimarket.polimarket_backend.bodega.model.Stock;
import com.polimarket.polimarket_backend.bodega.repository.ProductoRepository;
import com.polimarket.polimarket_backend.bodega.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DisponibilidadTests {
    @Autowired ProductoRepository productos;
    @Autowired StockRepository stocks;
    @Autowired IBodegaFacade fachada;
    @Autowired Environment environment;

    private HttpResponse<String> consultar(String ruta) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"
                    + environment.getProperty("local.server.port") + "/api/bodega/productos/" + ruta))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    void sirveLaPlataformaWebYLosRecursos() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String base = "http://localhost:" + environment.getProperty("local.server.port");
            for (String ruta : new String[]{"/", "/styles.css", "/app.js"}) {
                HttpResponse<String> respuesta = client.send(HttpRequest.newBuilder(URI.create(base + ruta))
                        .GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, respuesta.statusCode(), ruta);
                assertFalse(respuesta.body().isBlank(), ruta);
                if (ruta.equals("/")) {
                    assertTrue(respuesta.body().contains("Portal de vendedores"));
                    assertTrue(respuesta.body().contains("id=\"seller-form\""));
                }
            }
        }
    }

    @Test
    void consultaPorHttpSinModificarInventarioYValidaParametros() throws Exception {
        Producto producto = new Producto();
        producto.setNombre("Producto RF-02");
        producto = productos.saveAndFlush(producto);
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(10);
        stock = stocks.saveAndFlush(stock);
        Long productoId = producto.getId();
        try {
            HttpResponse<String> disponible = consultar(productoId + "/disponibilidad?cantidad=10");
            assertEquals(200, disponible.statusCode());
            assertTrue(disponible.body().contains("\"disponible\":true"));
            assertTrue(consultar(productoId + "/disponibilidad?cantidad=11").body()
                    .contains("\"disponible\":false"));
            assertEquals(10, stocks.findByProductoId(productoId).orElseThrow().getCantidad());
            assertEquals(400, consultar(productoId + "/disponibilidad?cantidad=0").statusCode());
            assertEquals(400, consultar(productoId + "/disponibilidad?cantidad=-1").statusCode());
            assertEquals(400, consultar("0/disponibilidad?cantidad=1").statusCode());
            assertEquals(400, consultar(productoId + "/disponibilidad").statusCode());
            assertEquals(400, consultar(productoId + "/disponibilidad?cantidad=abc").statusCode());
            assertThrows(IllegalArgumentException.class, () -> fachada.consultarDisponibilidad(null, 1));
            stocks.deleteById(stock.getId());
            assertFalse(fachada.consultarDisponibilidad(productoId, 1));
            productos.deleteById(productoId);
            HttpResponse<String> inexistente = consultar(productoId + "/disponibilidad?cantidad=1");
            assertEquals(200, inexistente.statusCode());
            assertTrue(inexistente.body().contains("\"disponible\":false"));
        } finally {
            stocks.findByProductoId(productoId).ifPresent(stocks::delete);
            if (productos.existsById(productoId)) productos.deleteById(productoId);
        }
    }
}
