package com.tusistema.sistemaventas.model;

// Esta es una clase simple para guardar los datos de configuración de la empresa.
public class DatosEmpresa {

    private String monedaPorDefecto = "COP";
    private Double porcentajeIva = 19.0;

    // --- Constructores ---
    public DatosEmpresa() {
    }

    public DatosEmpresa(String monedaPorDefecto, Double porcentajeIva) {
        this.monedaPorDefecto = monedaPorDefecto;
        this.porcentajeIva = porcentajeIva;
    }

    // --- Getters y Setters ---
    public String getMonedaPorDefecto() {
        return monedaPorDefecto;
    }

    public void setMonedaPorDefecto(String monedaPorDefecto) {
        this.monedaPorDefecto = monedaPorDefecto;
    }

    public Double getPorcentajeIva() {
        return porcentajeIva;
    }

    public void setPorcentajeIva(Double porcentajeIva) {
        this.porcentajeIva = porcentajeIva;
    }
}