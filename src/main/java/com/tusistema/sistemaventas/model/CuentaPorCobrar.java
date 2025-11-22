package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "cuentas_por_cobrar")
public class CuentaPorCobrar {

    @Id
    private String id;
    private String ventaId;
    private String clienteId;
    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private LocalDate fechaDeVencimiento;
    private EstadoCuenta estado;

    public enum EstadoCuenta {
        PENDIENTE,
        PAGADA,
        VENCIDA,
        ANULADA // <-- NUEVO ESTADO AÑADIDO
    }

    // Constructor vacío
    public CuentaPorCobrar() {
    }

    // Getters y Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
    public LocalDate getFechaDeVencimiento() { return fechaDeVencimiento; }
    public void setFechaDeVencimiento(LocalDate fechaDeVencimiento) { this.fechaDeVencimiento = fechaDeVencimiento; }
    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta estado) { this.estado = estado; }
}