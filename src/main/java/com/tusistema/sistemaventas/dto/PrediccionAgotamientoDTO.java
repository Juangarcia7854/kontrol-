package com.tusistema.sistemaventas.dto;

/**
 * DTO para enviar el resultado de la clasificación de Weka al frontend.
 */
public class PrediccionAgotamientoDTO {

    private String productoId;
    private String prediccion; // "SI", "NO", o "ERROR"
    private String mensajeError; // Opcional, si algo falla

    public PrediccionAgotamientoDTO(String productoId, String prediccion, String mensajeError) {
        this.productoId = productoId;
        this.prediccion = prediccion;
        this.mensajeError = mensajeError;
    }

    public PrediccionAgotamientoDTO(String productoId, String prediccion) {
        this(productoId, prediccion, null);
    }

    // Getters y Setters
    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getPrediccion() {
        return prediccion;
    }

    public void setPrediccion(String prediccion) {
        this.prediccion = prediccion;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }
}