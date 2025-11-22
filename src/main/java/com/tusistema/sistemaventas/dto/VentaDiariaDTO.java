package com.tusistema.sistemaventas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VentaDiariaDTO {
    
    private LocalDate fecha;
    private BigDecimal totalVenta;
    private int cantidadItems;

    // Constructores
    public VentaDiariaDTO() {
    }

    public VentaDiariaDTO(LocalDate fecha, BigDecimal totalVenta, int cantidadItems) {
        this.fecha = fecha;
        this.totalVenta = totalVenta;
        this.cantidadItems = cantidadItems;
    }

    // Getters y Setters
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getTotalVenta() { return totalVenta; }
    public void setTotalVenta(BigDecimal totalVenta) { this.totalVenta = totalVenta; }
    public int getCantidadItems() { return cantidadItems; }
    public void setCantidadItems(int cantidadItems) { this.cantidadItems = cantidadItems; }
}