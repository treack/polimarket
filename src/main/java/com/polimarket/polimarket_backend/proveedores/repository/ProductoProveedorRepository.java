package com.polimarket.polimarket_backend.proveedores.repository;

import com.polimarket.polimarket_backend.proveedores.model.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.polimarket.polimarket_backend.proveedores.dto.ProveedorDto;
import java.util.List;

public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {

    List<ProductoProveedor> findByProductoId(Long productoId);

    @Query("""
            select new com.polimarket.polimarket_backend.proveedores.dto.ProveedorDto(
                proveedor.id, proveedor.nombre)
            from ProductoProveedor relacion join relacion.proveedor proveedor
            where relacion.producto.id = :productoId
            order by proveedor.id
            """)
    List<ProveedorDto> consultarProveedores(
            @Param("productoId") Long productoId);
}
