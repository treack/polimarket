package com.polimarket.polimarket_backend.ventas.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.polimarket.polimarket_backend.rrhh.model.Vendedor;

/** Modelo de dominio de ventas; entidad persistente JPA. */
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Vendedor vendedor;
    private java.time.LocalDateTime fecha;
    private String direccion;
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public EstadoPedido getEstado() { return estado == null ? EstadoPedido.PENDIENTE : estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pedido_id", nullable = false)
    private java.util.List<DetallePedido> detalles = new java.util.ArrayList<>();

    public Pedido() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public java.util.List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(java.util.List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
