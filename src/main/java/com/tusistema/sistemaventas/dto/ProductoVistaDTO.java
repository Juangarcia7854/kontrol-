package com.tusistema.sistemaventas.dto;

import com.tusistema.sistemaventas.model.Producto;
import java.math.BigDecimal;

public class ProductoVistaDTO {

    private String id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoria;
    private String imagenUrl;
    private int stockActual;

    public static ProductoVistaDTO fromProducto(Producto producto, int stock) {
        ProductoVistaDTO dto = new ProductoVistaDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setCategoria(producto.getCategoria());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setStockActual(stock);
        return dto;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
}