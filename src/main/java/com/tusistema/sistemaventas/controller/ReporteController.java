package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.service.ReporteService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/reportes") // O "/graficas" si prefieres
// --- ⬇️ ¡AQUÍ ESTÁ LA CORRECCIÓN! ⬇️ ---
// Se añade 'ROLE_DEMO' para permitir el acceso a la demo
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEMO')")
public class ReporteController {

    private final ReporteService reporteService;

    @Autowired
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public String mostrarDashboardReportes(Model model) {
        // 1. Datos para Ventas Diarias (últimos 7 días)
        int diasVentas = 7;
        Map<String, BigDecimal> ventasDiarias = reporteService.obtenerVentasDiariasUltimosXDias(diasVentas);
        model.addAttribute("labelsVentasDiarias", new ArrayList<>(ventasDiarias.keySet()));
        model.addAttribute("dataVentasDiarias", new ArrayList<>(ventasDiarias.values()));
        model.addAttribute("tituloVentasDiarias", "Ventas Totales de los Últimos " + diasVentas + " Días");

        // 2. Datos para Top 5 Productos Más Vendidos (por cantidad)
        int topNProductos = 5;
        Map<String, Integer> topProductos = reporteService.obtenerTopNProductosMasVendidosPorCantidad(topNProductos);
        model.addAttribute("labelsTopProductos", new ArrayList<>(topProductos.keySet()));
        model.addAttribute("dataTopProductos", new ArrayList<>(topProductos.values()));
        model.addAttribute("tituloTopProductos", "Top " + topNProductos + " Productos Más Vendidos (por Cantidad)");

        model.addAttribute("pageTitle", "Dashboard de Reportes");
        return "reportes/dashboard-reportes"; // Vista: src/main/resources/templates/reportes/dashboard-reportes.html
    }

    // Exportar reporte de ventas en PDF
    @GetMapping("/exportar/ventas-pdf")
    public void exportarVentasPdf(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fechaActual = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=ReporteVentas_" + fechaActual + ".pdf";
            response.setHeader(headerKey, headerValue);

            reporteService.generarReporteVentasPdf(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Exportar reporte de ventas en Excel
    @GetMapping("/exportar/ventas-excel")
    public void exportarVentasExcel(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fechaActual = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=ReporteVentas_" + fechaActual + ".xlsx";
            response.setHeader(headerKey, headerValue);

            reporteService.generarReporteVentasExcel(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}