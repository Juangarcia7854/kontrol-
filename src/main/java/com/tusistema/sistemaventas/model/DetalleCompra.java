package com.tusistema.sistemaventas.model;

import java.math.BigDecimal;

/**
 * Representa un producto individual dentro de una Compra.
 * Se utiliza como subdocumento incrustado dentro de la clase Compra.
 */
public class DetalleCompra {

    private String productoId; 
    private String nombreProducto; 

    private int cantidad;
    private BigDecimal precioUnitario; 
    private BigDecimal subtotal;

    // Constructor vacío (Necesario para frameworks como Spring/Jackson)
    public DetalleCompra() {
    }

    // Getters y Setters

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}