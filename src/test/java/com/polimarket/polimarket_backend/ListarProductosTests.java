package com.polimarket.polimarket_backend;

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
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:rf05;MODE=MySQL;DB_CLOSE_DELAY=-1")
class ListarProductosTests {
    @Autowired ProductoRepository productos;
    @Autowired StockRepository stocks;
    @Autowired Environment environment;

    @Test
    void listaSoloStockPositivoYDevuelveListaVaciaSinExistencias() throws Exception {
        List<Long> ids = new ArrayList<>();
        List<Long> stockIds = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:" + environment.getProperty("local.server.port")
                    + "/api/ventas/productos");
            HttpRequest solicitud = HttpRequest.newBuilder(url).GET().build();
            assertEquals("[]", client.send(solicitud, HttpResponse.BodyHandlers.ofString()).body());
            for (int cantidad : new int[]{12, 0, -1}) {
                Producto producto = new Producto();
                producto.setNombre("Producto " + cantidad);
                producto = productos.saveAndFlush(producto);
                ids.add(producto.getId());
                Stock stock = new Stock();
                stock.setProducto(producto);
                stock.setCantidad(cantidad);
                stockIds.add(stocks.saveAndFlush(stock).getId());
            }
            Producto sinStock = new Producto();
            sinStock.setNombre("Sin registro de stock");
            ids.add(productos.saveAndFlush(sinStock).getId());

            HttpResponse<String> respuesta = client.send(solicitud, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, respuesta.statusCode());
            assertTrue(respuesta.headers().firstValue("content-type").orElseThrow().contains("application/json"));
            assertEquals("[{\"id\":" + ids.getFirst()
                    + ",\"nombre\":\"Producto 12\",\"existencias\":12}]", respuesta.body());
            assertEquals(12, stocks.findByProductoId(ids.getFirst()).orElseThrow().getCantidad());
            Stock agotado = stocks.findById(stockIds.getFirst()).orElseThrow();
            agotado.setCantidad(0);
            stocks.saveAndFlush(agotado);
            assertEquals("[]", client.send(solicitud, HttpResponse.BodyHandlers.ofString()).body());
        } finally {
            stockIds.forEach(stocks::deleteById);
            ids.forEach(productos::deleteById);
        }
    }
}
