package com.polimarket.polimarket_backend.bodega.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

/** Movimiento de inventario RF-13; independiente de la entrega de un pedido. */
@Entity
@Table(name = "salidas_inventario")
public class SalidaInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(nullable = false)
    private int cantidad;
    @Column(nullable = false)
    private LocalDateTime fecha;
    private Long pedidoId;

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }


    protected SalidaInventario() { }

    public SalidaInventario(Producto producto, int cantidad, LocalDateTime fecha) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
}
