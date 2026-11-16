package com.polimarket.polimarket_backend.bodega.repository;

import java.util.List;
import com.polimarket.polimarket_backend.bodega.dto.ProductoInventarioDto;
import org.springframework.data.jpa.repository.Query;

import com.polimarket.polimarket_backend.bodega.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

    java.util.Optional<Stock> findByProductoId(Long productoId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.producto.id = :productoId")
    java.util.Optional<Stock> buscarParaSalida(
            @org.springframework.data.repository.query.Param("productoId") Long productoId);

    @Query("""
            select new com.polimarket.polimarket_backend.bodega.dto.ProductoInventarioDto(
                p.id, p.nombre, s.cantidad)
            from Stock s join s.producto p
            where s.cantidad > 0
            order by p.id
            """)
    List<ProductoInventarioDto> listarProductosDisponibles();
}
