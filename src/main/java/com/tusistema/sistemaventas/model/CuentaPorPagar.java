package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "cuentas_por_pagar")
public class CuentaPorPagar {

    @Id
    private String id;
    private String compraId;
    private String proveedorId;
    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private LocalDate fechaDeVencimiento;
    private EstadoCuenta estado;

    public enum EstadoCuenta {
        PENDIENTE,
        PAGADA,
        VENCIDA,
        ANULADA // Estado requerido
    }

    // Constructor vacío
    public CuentaPorPagar() {
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompraId() { return compraId; }
    public void setCompraId(String compraId) { this.compraId = compraId; }
    public String getProveedorId() { return proveedorId; }
    public void setProveedorId(String proveedorId) { this.proveedorId = proveedorId; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
    public LocalDate getFechaDeVencimiento() { return fechaDeVencimiento; }
    public void setFechaDeVencimiento(LocalDate fechaDeVencimiento) { this.fechaDeVencimiento = fechaDeVencimiento; }
    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta estado) { this.estado = estado; }
}