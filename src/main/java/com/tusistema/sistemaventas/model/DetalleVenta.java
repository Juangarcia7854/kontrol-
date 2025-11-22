package com.tusistema.sistemaventas.model; // Reemplaza con tu paquete

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

// No necesita @Document ya que será embebida
// Si usas Lombok:
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class DetalleVenta {

    @NotBlank(message = "El ID del producto es obligatorio en el detalle.")
    private String productoId; // ID del Producto vendido

    @NotBlank(message = "El nombre del producto es obligatorio en el detalle.")
    private String nombreProducto; // Nombre del producto en el momento de la venta

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio.")
    private BigDecimal precioUnitario; // Precio del producto en el momento de la venta

    @NotNull(message = "El subtotal es obligatorio.")
    private BigDecimal subtotal; // cantidad * precioUnitario

    // Constructores
    public DetalleVenta() {
    }

    public DetalleVenta(String productoId, String nombreProducto, Integer cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        // Recalcular subtotal si la cantidad cambia y el precio está seteado
        if (this.precioUnitario != null) {
            this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        // Recalcular subtotal si el precio cambia y la cantidad está seteada
        if (this.cantidad != null) {
            this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
