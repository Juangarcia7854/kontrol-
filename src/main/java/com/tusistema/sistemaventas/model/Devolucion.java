package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "devoluciones")
public class Devolucion {

    @Id
    private String id;

    @NotNull(message = "La fecha de la devolución es obligatoria.")
    private LocalDateTime fechaDevolucion;

    @NotNull(message = "La venta original es obligatoria para la devolución.")
    private String ventaOriginalId;
    private String numeroFacturaVentaOriginal;

    private String clienteId;
    private String nombreCliente;

    private String usuarioId;
    private String usernameUsuario;

    @NotEmpty(message = "Una devolución debe tener al menos un detalle de producto.")
    private List<DetalleDevolucion> detalles = new ArrayList<>();

    @NotNull(message = "El total de la devolución es obligatorio.")
    private BigDecimal totalDevolucion;

    private String estadoDevolucion;
    private String tipoReembolso;
    private BigDecimal montoReembolsado;
    private String notas;

    public Devolucion() {
        this.fechaDevolucion = LocalDateTime.now();
        this.totalDevolucion = BigDecimal.ZERO;
        this.estadoDevolucion = "PENDIENTE";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getVentaOriginalId() { return ventaOriginalId; }
    public void setVentaOriginalId(String ventaOriginalId) { this.ventaOriginalId = ventaOriginalId; }

    public String getNumeroFacturaVentaOriginal() { return numeroFacturaVentaOriginal; }
    public void setNumeroFacturaVentaOriginal(String numeroFacturaVentaOriginal) { this.numeroFacturaVentaOriginal = numeroFacturaVentaOriginal; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsernameUsuario() { return usernameUsuario; }
    public void setUsernameUsuario(String usernameUsuario) { this.usernameUsuario = usernameUsuario; }

    public List<DetalleDevolucion> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleDevolucion> detalles) {
        this.detalles = Objects.requireNonNullElseGet(detalles, ArrayList::new);
    }

    public BigDecimal getTotalDevolucion() {
        return Objects.requireNonNullElse(this.totalDevolucion, BigDecimal.ZERO);
    }
    public void setTotalDevolucion(BigDecimal totalDevolucion) {
        this.totalDevolucion = Objects.requireNonNullElse(totalDevolucion, BigDecimal.ZERO);
    }

    public String getEstadoDevolucion() { return estadoDevolucion; }
    public void setEstadoDevolucion(String estadoDevolucion) { this.estadoDevolucion = estadoDevolucion; }

    public String getTipoReembolso() { return tipoReembolso; }
    public void setTipoReembolso(String tipoReembolso) { this.tipoReembolso = tipoReembolso; }

    public BigDecimal getMontoReembolsado() { return montoReembolsado; }
    public void setMontoReembolsado(BigDecimal montoReembolsado) { this.montoReembolsado = montoReembolsado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public void calcularTotalDevolucion() {
        if (this.detalles == null) {
            this.totalDevolucion = BigDecimal.ZERO;
            return;
        }
        this.totalDevolucion = this.detalles.stream()
                                    .map(DetalleDevolucion::getSubtotalDevolucion)
                                    .filter(Objects::nonNull)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}