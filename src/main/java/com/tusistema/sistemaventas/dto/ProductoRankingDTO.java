package com.tusistema.sistemaventas.dto;

public class ProductoRankingDTO {
    private String nombreProducto;
    private int cantidadVendida;

    public ProductoRankingDTO(String nombreProducto, int cantidadVendida) {
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
    }
    // Getters y Setters
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(int cantidadVendida) { this.cantidadVendida = cantidadVendida; }
}