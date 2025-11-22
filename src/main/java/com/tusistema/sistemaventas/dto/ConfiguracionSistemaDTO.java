package com.tusistema.sistemaventas.dto;

public class ConfiguracionSistemaDTO {

    private String id;

    private String nombreEmpresa;
    private String direccion;
    private String nitRucEmpresa;
    private String telefono;
    private String emailContacto;

    private String monedaPorDefecto;
    private String idiomaPorDefecto;

    public ConfiguracionSistemaDTO() {
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNitRucEmpresa() {
        return nitRucEmpresa;
    }

    public void setNitRucEmpresa(String nitRucEmpresa) {
        this.nitRucEmpresa = nitRucEmpresa;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getMonedaPorDefecto() {
        return monedaPorDefecto;
    }

    public void setMonedaPorDefecto(String monedaPorDefecto) {
        this.monedaPorDefecto = monedaPorDefecto;
    }

    public String getIdiomaPorDefecto() {
        return idiomaPorDefecto;
    }

    public void setIdiomaPorDefecto(String idiomaPorDefecto) {
        this.idiomaPorDefecto = idiomaPorDefecto;
    }

    @Override
    public String toString() {
        return "ConfiguracionSistema{" +
                "id='" + id + '\'' +
                ", nombreEmpresa='" + nombreEmpresa + '\'' +
                ", direccion='" + direccion + '\'' +
                ", nitRucEmpresa='" + nitRucEmpresa + '\'' +
                ", telefono='" + telefono + '\'' +
                ", emailContacto='" + emailContacto + '\'' +
                ", monedaPorDefecto='" + monedaPorDefecto + '\'' +
                ", idiomaPorDefecto='" + idiomaPorDefecto + '\'' +
                '}';
    }
}