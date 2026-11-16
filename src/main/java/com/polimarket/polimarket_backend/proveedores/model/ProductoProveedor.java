package com.polimarket.polimarket_backend.proveedores.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.polimarket.polimarket_backend.bodega.model.Producto;

/** Modelo de dominio de proveedores; entidad persistente JPA. */
@Entity
@Table(name = "productos_proveedores", uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "proveedor_id"}))
public class ProductoProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;
    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal precioCompra;

    public ProductoProveedor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public java.math.BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(java.math.BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }
}
