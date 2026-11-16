package com.polimarket.polimarket_backend.bodega.repository;

import com.polimarket.polimarket_backend.bodega.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
