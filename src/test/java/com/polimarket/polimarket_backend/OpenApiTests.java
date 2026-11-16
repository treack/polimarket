package com.polimarket.polimarket_backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiTests {
    @Autowired Environment environment;

    @Test
    void publicaLosCincoRequerimientosYLaInterfazSwagger() throws Exception {
        String base = "http://localhost:" + environment.getProperty("local.server.port");
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL).build()) {
            HttpResponse<String> documento = client.send(HttpRequest.newBuilder(
                    URI.create(base + "/v3/api-docs")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, documento.statusCode());
            var json = JsonMapper.builder().build().readTree(documento.body());
            assertEquals("PoliMarket API", json.path("info").path("title").asText());
            var paths = json.path("paths");
            assertEquals(8, paths.size());
            assertTrue(paths.path("/api/ventas/pedidos").has("post"));
            assertTrue(paths.path("/api/ventas/pedidos").has("get"));
            assertTrue(paths.path("/api/entregas/pedidos/{pedidoId}").has("post"));
            assertTrue(paths.has("/api/ventas/productos"));
            assertTrue(paths.has("/api/bodega/productos/{productoId}/disponibilidad"));
            assertTrue(paths.has("/api/rrhh/vendedores/{vendedorId}/autorizacion"));
            assertTrue(paths.has("/api/proveedores/productos/{productoId}"));
            var salida = paths.path("/api/entregas/salidas").path("post");
            assertTrue(salida.path("summary").asText().contains("RF-13"));
            assertTrue(salida.path("responses").has("400"));
            assertTrue(salida.path("responses").has("409"));
            assertTrue(salida.has("requestBody"));
            assertTrue(json.path("components").path("schemas").has("RegistrarSalidaDto"));

            for (String recurso : new String[]{"/swagger-ui.html", "/swagger-ui/swagger-ui-bundle.js",
                    "/v3/api-docs/swagger-config", "/v3/api-docs.yaml"}) {
                HttpResponse<String> respuesta = client.send(HttpRequest.newBuilder(
                        URI.create(base + recurso)).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, respuesta.statusCode(), recurso);
                assertFalse(respuesta.body().isBlank(), recurso);
                if (recurso.equals("/swagger-ui.html")) assertTrue(respuesta.body().contains("Swagger UI"));
            }
        }
    }
}
