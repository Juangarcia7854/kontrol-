package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    private String id;
    private LocalDateTime fecha;
    private String productoId;
    
    /**
     * TIPO DE CAMPO CORREGIDO:
     * Ahora es un String para guardar el username (ej. "admin")
     * que le pasamos desde el InventarioController.
     */
    private String usuarioId; // <-- ESTA ES LA CORRECCIÓN CLAVE

    private String tipoMovimiento; // "VENTA", "COMPRA", "AJUSTE_MANUAL", "DEVOLUCION"
    private String motivo; // "Venta #F-001", "Ajuste por conteo"
    
    private int cantidad; // Ej: 50 (para entrada), -10 (para salida)
    private int stockAnterior;
    private int stockNuevo;

    // --- Constantes para los tipos de movimiento ---
    public static final String TIPO_VENTA = "VENTA";
    public static final String TIPO_COMPRA = "COMPRA";
    public static final String TIPO_AJUSTE_MANUAL = "AJUSTE_MANUAL";
    public static final String TIPO_DEVOLUCION = "DEVOLUCION";
    
    // ============================================
    // ==     CONSTANTE AÑADIDA PARA CORREGIR    ==
    // ============================================
    public static final String TIPO_INICIAL = "INICIAL"; // <-- LÍNEA AÑADIDA

    // Constructor
    public MovimientoInventario() {
        this.fecha = LocalDateTime.now();
    }

    // --- Getters y Setters ---
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(int stockNuevo) {
        this.stockNuevo = stockNuevo;
    }
}