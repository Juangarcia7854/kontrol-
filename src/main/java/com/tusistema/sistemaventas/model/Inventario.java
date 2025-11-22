package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "inventario")
public class Inventario {

    @Id
    private String id;

    @Indexed(unique = true) // Solo puede haber un registro de inventario por producto
    private String productoId;
    
    private int cantidad;
    
    private LocalDateTime fechaUltimaActualizacion;

    public Inventario() {
    }

    public Inventario(String productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFechaUltimaActualizacion() { return fechaUltimaActualizacion; }
    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) { this.fechaUltimaActualizacion = fechaUltimaActualizacion; }
}