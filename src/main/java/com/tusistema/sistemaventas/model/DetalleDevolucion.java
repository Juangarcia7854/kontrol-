package com.tusistema.sistemaventas.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects; // Para Objects.requireNonNullElse

public class DetalleDevolucion {

    @NotBlank(message = "El ID del producto es obligatorio en el detalle de devolución.")
    private String productoId;

    @NotBlank(message = "El nombre del producto es obligatorio en el detalle de devolución.")
    private String nombreProducto;

    @NotNull(message = "La cantidad devuelta es obligatoria.")
    @Min(value = 1, message = "La cantidad devuelta debe ser al menos 1.")
    private Integer cantidadDevuelta; // Se espera que se llene en el formulario

    @NotNull(message = "El precio unitario de devolución es obligatorio.")
    private BigDecimal precioUnitarioDevolucion;

    @NotNull(message = "El subtotal de la devolución es obligatorio.")
    private BigDecimal subtotalDevolucion;

    private String motivoDevolucion;

    // Constructores
    public DetalleDevolucion() {
        // Inicializar valores BigDecimal a ZERO para evitar NullPointerExceptions
        this.precioUnitarioDevolucion = BigDecimal.ZERO;
        this.subtotalDevolucion = BigDecimal.ZERO;
        // cantidadDevuelta será null hasta que se ingrese en el formulario
    }

    public DetalleDevolucion(String productoId, String nombreProducto, Integer cantidadDevuelta, BigDecimal precioUnitarioDevolucion, String motivoDevolucion) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidadDevuelta = Objects.requireNonNullElse(cantidadDevuelta, 0); // Default a 0 si es null
        this.precioUnitarioDevolucion = Objects.requireNonNullElse(precioUnitarioDevolucion, BigDecimal.ZERO);
        this.motivoDevolucion = motivoDevolucion;
        // Calcular subtotal
        this.subtotalDevolucion = this.precioUnitarioDevolucion.multiply(new BigDecimal(this.cantidadDevuelta));
    }

    // Getters y Setters
    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Integer getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public void setCantidadDevuelta(Integer cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
        recalcularSubtotal();
    }

    public BigDecimal getPrecioUnitarioDevolucion() {
        return precioUnitarioDevolucion;
    }

    public void setPrecioUnitarioDevolucion(BigDecimal precioUnitarioDevolucion) {
        this.precioUnitarioDevolucion = Objects.requireNonNullElse(precioUnitarioDevolucion, BigDecimal.ZERO);
        recalcularSubtotal();
    }

    public BigDecimal getSubtotalDevolucion() {
        // Asegurar que nunca devuelva null
        return Objects.requireNonNullElse(this.subtotalDevolucion, BigDecimal.ZERO);
    }

    public void setSubtotalDevolucion(BigDecimal subtotalDevolucion) {
        this.subtotalDevolucion = Objects.requireNonNullElse(subtotalDevolucion, BigDecimal.ZERO);
    }

    public String getMotivoDevolucion() {
        return motivoDevolucion;
    }

    public void setMotivoDevolucion(String motivoDevolucion) {
        this.motivoDevolucion = motivoDevolucion;
    }

    private void recalcularSubtotal() {
        if (this.precioUnitarioDevolucion != null && this.cantidadDevuelta != null && this.cantidadDevuelta >= 0) {
            this.subtotalDevolucion = this.precioUnitarioDevolucion.multiply(new BigDecimal(this.cantidadDevuelta));
        } else {
            this.subtotalDevolucion = BigDecimal.ZERO; // Default a cero si no se puede calcular
        }
    }
}