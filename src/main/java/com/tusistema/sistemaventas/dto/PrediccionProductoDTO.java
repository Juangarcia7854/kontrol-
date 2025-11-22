package com.tusistema.sistemaventas.dto;

import com.tusistema.sistemaventas.model.Producto;

public class PrediccionProductoDTO {

    private Producto producto;
    private String detalles;
    
    // Resultados (SI/NO)
    private String seVendePronto;
    private String riesgoDevolucion;
    private String necesitaPromocion;
    
    // --- NUEVOS CAMPOS (CONFIDENCIA Y RECOMENDACIONES) ---
    private String confianzaVenta;
    private String confianzaDevolucion;
    private String confianzaPromocion;

    private String recomendacionVenta;
    private String recomendacionDevolucion;
    private String recomendacionPromocion;
    // -----------------------------------------------------

    private String error;

    public PrediccionProductoDTO(Producto producto) {
        this.producto = producto;
    }

    // --- Getters y Setters ---
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    
    public String getSeVendePronto() { return seVendePronto; }
    public void setSeVendePronto(String seVendePronto) { this.seVendePronto = seVendePronto; }
    
    public String getRiesgoDevolucion() { return riesgoDevolucion; }
    public void setRiesgoDevolucion(String riesgoDevolucion) { this.riesgoDevolucion = riesgoDevolucion; }
    
    public String getNecesitaPromocion() { return necesitaPromocion; }
    public void setNecesitaPromocion(String necesitaPromocion) { this.necesitaPromocion = necesitaPromocion; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    // --- Nuevos Getters y Setters ---
    public String getConfianzaVenta() { return confianzaVenta; }
    public void setConfianzaVenta(String confianzaVenta) { this.confianzaVenta = confianzaVenta; }

    public String getConfianzaDevolucion() { return confianzaDevolucion; }
    public void setConfianzaDevolucion(String confianzaDevolucion) { this.confianzaDevolucion = confianzaDevolucion; }

    public String getConfianzaPromocion() { return confianzaPromocion; }
    public void setConfianzaPromocion(String confianzaPromocion) { this.confianzaPromocion = confianzaPromocion; }

    public String getRecomendacionVenta() { return recomendacionVenta; }
    public void setRecomendacionVenta(String recomendacionVenta) { this.recomendacionVenta = recomendacionVenta; }

    public String getRecomendacionDevolucion() { return recomendacionDevolucion; }
    public void setRecomendacionDevolucion(String recomendacionDevolucion) { this.recomendacionDevolucion = recomendacionDevolucion; }

    public String getRecomendacionPromocion() { return recomendacionPromocion; }
    public void setRecomendacionPromocion(String recomendacionPromocion) { this.recomendacionPromocion = recomendacionPromocion; }
}