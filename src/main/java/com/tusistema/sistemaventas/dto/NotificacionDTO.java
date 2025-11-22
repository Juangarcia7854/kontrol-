package com.tusistema.sistemaventas.dto;

public class NotificacionDTO {
    private String tipo;
    private String mensaje;

    public NotificacionDTO(String tipo, String mensaje) {
        this.tipo = tipo;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}