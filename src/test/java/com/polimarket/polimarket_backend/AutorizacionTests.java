package com.polimarket.polimarket_backend;

import com.polimarket.polimarket_backend.rrhh.facade.IRRHHFacade;
import com.polimarket.polimarket_backend.rrhh.model.Empleado;
import com.polimarket.polimarket_backend.rrhh.model.Vendedor;
import com.polimarket.polimarket_backend.rrhh.repository.EmpleadoRepository;
import com.polimarket.polimarket_backend.rrhh.repository.VendedorRepository;
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
        properties = "spring.datasource.url=jdbc:h2:mem:rf10;MODE=MySQL;DB_CLOSE_DELAY=-1")
class AutorizacionTests {
    @Autowired EmpleadoRepository empleados;
    @Autowired VendedorRepository vendedores;
    @Autowired IRRHHFacade fachada;
    @Autowired Environment environment;

    private HttpResponse<String> consultar(HttpClient client, String id) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"
                + environment.getProperty("local.server.port") + "/api/rrhh/vendedores/"
                + id + "/autorizacion")).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void requiereVendedorAutorizadoYEmpleadoActivo() throws Exception {
        List<Long> empleadoIds = new ArrayList<>();
        List<Long> vendedorIds = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (boolean activo : new boolean[]{true, false}) {
                for (boolean autorizado : new boolean[]{true, false}) {
                    Empleado empleado = new Empleado();
                    empleado.setActivo(activo);
                    empleado = empleados.saveAndFlush(empleado);
                    empleadoIds.add(empleado.getId());
                    Vendedor vendedor = new Vendedor();
                    vendedor.setEmpleado(empleado);
                    vendedor.setAutorizado(autorizado);
                    vendedor = vendedores.saveAndFlush(vendedor);
                    vendedorIds.add(vendedor.getId());
                    HttpResponse<String> respuesta = consultar(client, vendedor.getId().toString());
                    assertEquals(200, respuesta.statusCode());
                    assertEquals("{\"vendedorId\":" + vendedor.getId()
                            + ",\"autorizado\":" + (activo && autorizado) + "}", respuesta.body());
                }
            }
            HttpResponse<String> inexistente = consultar(client, Long.toString(Long.MAX_VALUE));
            assertEquals(200, inexistente.statusCode());
            assertTrue(inexistente.body().contains("\"autorizado\":false"));
            assertEquals(400, consultar(client, "0").statusCode());
            assertEquals(400, consultar(client, "-1").statusCode());
            assertEquals(400, consultar(client, "abc").statusCode());
            assertThrows(IllegalArgumentException.class, () -> fachada.validarAutorizacion(null));
            assertEquals(4, vendedores.findAllById(vendedorIds).size());
        } finally {
            vendedorIds.forEach(vendedores::deleteById);
            empleadoIds.forEach(empleados::deleteById);
        }
    }
}
