package com.polimarket.polimarket_backend.bodega.repository;

import com.polimarket.polimarket_backend.bodega.model.SalidaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalidaInventarioRepository extends JpaRepository<SalidaInventario, Long> {
    List<SalidaInventario> findByProductoId(Long productoId);
}
