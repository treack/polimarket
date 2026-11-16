package com.polimarket.polimarket_backend.ventas.repository;

import com.polimarket.polimarket_backend.ventas.model.Pedido;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findTop100ByVendedorIdOrderByIdDesc(Long vendedorId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :pedidoId")
    Optional<Pedido> buscarParaDespacho(@Param("pedidoId") Long pedidoId);
}
