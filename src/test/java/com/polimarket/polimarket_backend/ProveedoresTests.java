package com.polimarket.polimarket_backend;

import com.polimarket.polimarket_backend.bodega.model.Producto;
import com.polimarket.polimarket_backend.bodega.repository.ProductoRepository;
import com.polimarket.polimarket_backend.proveedores.facade.IProveedoresFacade;
import com.polimarket.polimarket_backend.proveedores.model.ProductoProveedor;
import com.polimarket.polimarket_backend.proveedores.model.Proveedor;
import com.polimarket.polimarket_backend.proveedores.repository.ProductoProveedorRepository;
import com.polimarket.polimarket_backend.proveedores.repository.ProveedorRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:rf12;MODE=MySQL;DB_CLOSE_DELAY=-1")
class ProveedoresTests {
    @Autowired ProductoRepository productos;
    @Autowired ProveedorRepository proveedores;
    @Autowired ProductoProveedorRepository relaciones;
    @Autowired IProveedoresFacade fachada;
    @Autowired Environment environment;

    private HttpResponse<String> consultar(HttpClient client, String id) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"
                + environment.getProperty("local.server.port") + "/api/proveedores/productos/" + id))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void consultaSoloLosProveedoresAsociadosYValidaIdentificador() throws Exception {
        List<Long> productoIds = new ArrayList<>();
        List<Long> proveedorIds = new ArrayList<>();
        List<Long> relacionIds = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (int i = 0; i < 2; i++) {
                Producto producto = new Producto();
                producto.setNombre("Producto " + i);
                productoIds.add(productos.saveAndFlush(producto).getId());
            }
            for (int i = 0; i < 3; i++) {
                Proveedor proveedor = new Proveedor();
                proveedor.setNombre("Proveedor " + i);
                proveedor = proveedores.saveAndFlush(proveedor);
                proveedorIds.add(proveedor.getId());
                ProductoProveedor relacion = new ProductoProveedor();
                relacion.setProducto(productos.findById(productoIds.get(i < 2 ? 0 : 1)).orElseThrow());
                relacion.setProveedor(proveedor);
                relacionIds.add(relaciones.saveAndFlush(relacion).getId());
            }
            HttpResponse<String> respuesta = consultar(client, productoIds.getFirst().toString());
            assertEquals(200, respuesta.statusCode());
            assertEquals("[{\"id\":" + proveedorIds.get(0) + ",\"nombre\":\"Proveedor 0\"},"
                    + "{\"id\":" + proveedorIds.get(1) + ",\"nombre\":\"Proveedor 1\"}]", respuesta.body());
            relaciones.deleteById(relacionIds.removeLast());
            assertEquals("[]", consultar(client, productoIds.get(1).toString()).body());
            HttpResponse<String> inexistente = consultar(client, Long.toString(Long.MAX_VALUE));
            assertEquals(200, inexistente.statusCode());
            assertEquals("[]", inexistente.body());
            assertEquals(400, consultar(client, "0").statusCode());
            assertEquals(400, consultar(client, "-1").statusCode());
            assertEquals(400, consultar(client, "abc").statusCode());
            assertThrows(IllegalArgumentException.class, () -> fachada.consultarProveedores(null));
            assertEquals(2, relaciones.findAllById(relacionIds).size());
        } finally {
            relacionIds.forEach(relaciones::deleteById);
            proveedorIds.forEach(proveedores::deleteById);
            productoIds.forEach(productos::deleteById);
        }
    }
}
