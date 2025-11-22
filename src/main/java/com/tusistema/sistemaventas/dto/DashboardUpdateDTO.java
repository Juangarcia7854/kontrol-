package com.tusistema.sistemaventas.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardUpdateDTO {
    private BigDecimal ingresosHoy;
    private int numeroVentasHoy;
    private BigDecimal ticketPromedio;
    private List<ProductoRankingDTO> rankingProductos;

    // ✅ INICIO: NUEVOS CAMPOS AÑADIDOS
    private String productoMasVendidoNombre;
    private Integer productoMasVendidoCantidad;
    private BigDecimal ventaPromedioPorCliente;
    // ✅ FIN: NUEVOS CAMPOS AÑADIDOS

    // Getters y Setters para todos los campos
    public BigDecimal getIngresosHoy() { return ingresosHoy; }
    public void setIngresosHoy(BigDecimal ingresosHoy) { this.ingresosHoy = ingresosHoy; }
    public int getNumeroVentasHoy() { return numeroVentasHoy; }
    public void setNumeroVentasHoy(int numeroVentasHoy) { this.numeroVentasHoy = numeroVentasHoy; }
    public BigDecimal getTicketPromedio() { return ticketPromedio; }
    public void setTicketPromedio(BigDecimal ticketPromedio) { this.ticketPromedio = ticketPromedio; }
    public List<ProductoRankingDTO> getRankingProductos() { return rankingProductos; }
    public void setRankingProductos(List<ProductoRankingDTO> rankingProductos) { this.rankingProductos = rankingProductos; }

    // ✅ INICIO: GETTERS Y SETTERS PARA NUEVOS CAMPOS
    public String getProductoMasVendidoNombre() { return productoMasVendidoNombre; }
    public void setProductoMasVendidoNombre(String productoMasVendidoNombre) { this.productoMasVendidoNombre = productoMasVendidoNombre; }
    public Integer getProductoMasVendidoCantidad() { return productoMasVendidoCantidad; }
    public void setProductoMasVendidoCantidad(Integer productoMasVendidoCantidad) { this.productoMasVendidoCantidad = productoMasVendidoCantidad; }
    public BigDecimal getVentaPromedioPorCliente() { return ventaPromedioPorCliente; }
    public void setVentaPromedioPorCliente(BigDecimal ventaPromedioPorCliente) { this.ventaPromedioPorCliente = ventaPromedioPorCliente; }
    // ✅ FIN: GETTERS Y SETTERS
}