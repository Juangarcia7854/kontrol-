package com.tusistema.sistemaventas.dto;

import java.time.LocalDate;

public class PrediccionInventarioDTO {

    private String productoId;
    private String nombreProducto;
    private int stockActual;
    private double ventaPromedioDiaria;
    private long diasRestantes;
    private LocalDate fechaAgotamiento;
    private String estado; // "OK", "Alerta", "Crítico"

    // Getters y Setters
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    public double getVentaPromedioDiaria() { return ventaPromedioDiaria; }
    public void setVentaPromedioDiaria(double ventaPromedioDiaria) { this.ventaPromedioDiaria = ventaPromedioDiaria; }
    public long getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(long diasRestantes) { this.diasRestantes = diasRestantes; }
    public LocalDate getFechaAgotamiento() { return fechaAgotamiento; }
    public void setFechaAgotamiento(LocalDate fechaAgotamiento) { this.fechaAgotamiento = fechaAgotamiento; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}