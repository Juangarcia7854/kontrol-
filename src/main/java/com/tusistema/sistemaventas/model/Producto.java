package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Document(collection = "productos")
public class Producto {

    @Id
    private String id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Indexed(unique = true)
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal precio;

    private String categoria;
    private String codigoBarras;
    private String imagenUrl;

    private int stock; // <-- CAMPO AÑADIDO

    @Transient
    private int stockInicial;

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
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockInicial() { return stockInicial; }
    public void setStockInicial(int stockInicial) { 
        this.stockInicial = stockInicial;
        if (this.stock == 0) {
            this.stock = stockInicial; 
        }
    }
}