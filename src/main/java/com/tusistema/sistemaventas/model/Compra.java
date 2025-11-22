package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // Necesario para inicializar
import java.util.List; // Necesario para la lista

@Document(collection = "compras")
public class Compra {

    @Id
    private String id;
    
    private String proveedorId; 
    private LocalDateTime fechaDeCompra;
    private BigDecimal total;

    // --- NUEVO CAMPO AGREGADO ---
    // Inicializar con ArrayList<> para evitar NullPointerException en Thymeleaf
    private List<DetalleCompra> detalles = new ArrayList<>(); 

    // Constructor vacío
    public Compra() {
    }

    // Getters y Setters correctos
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public LocalDateTime getFechaDeCompra() {
        return fechaDeCompra;
    }

    public void setFechaDeCompra(LocalDateTime fechaDeCompra) {
        this.fechaDeCompra = fechaDeCompra;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // --- NUEVOS GETTER Y SETTER PARA DETALLES ---
    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
}