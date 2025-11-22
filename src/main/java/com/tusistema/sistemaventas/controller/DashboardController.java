package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.dto.DashboardUpdateDTO;
import com.tusistema.sistemaventas.dto.GraficoDTO;
// import com.tusistema.sistemaventas.dto.PrediccionAgotamientoDTO; // <-- ELIMINADO
import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import com.tusistema.sistemaventas.service.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Currency;
import java.util.Locale;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final ConfiguracionSistemaService configuracionService;
    private final ReporteService reporteService;
    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(ConfiguracionSistemaService configuracionService,
                               ReporteService reporteService,
                               DashboardService dashboardService) {
        this.configuracionService = configuracionService;
        this.reporteService = reporteService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboardPrincipal(Model model, Authentication authentication, HttpSession session, Locale locale) {
        model.addAttribute("pageTitle", "Dashboard Principal");
        
        model.addAttribute("nombresTopProductos", reporteService.obtenerNombresTopProductosMasVendidos(5));
        model.addAttribute("cantidadesTopProductos", reporteService.obtenerCantidadesTopProductosMasVendidos(5));

        String currencySymbol;
        try {
            currencySymbol = Currency.getInstance(locale).getSymbol(locale);
        } catch (Exception e) {
            currencySymbol = "$";
        }
        model.addAttribute("monedaSimbolo", currencySymbol);

        ConfiguracionSistema datosEmpresa = configuracionService.obtenerConfiguracion();
        model.addAttribute("datosEmpresa", datosEmpresa != null ? datosEmpresa : new ConfiguracionSistema());
        
        return "admin/admin-dashboard-overview";
    }

    @GetMapping("/api/dashboard/metrics")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<DashboardUpdateDTO> getDashboardMetricsAjax() {
        return ResponseEntity.ok(dashboardService.obtenerMetricasActuales());
    }
    
    @GetMapping("/api/dashboard/ventas-netas")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<GraficoDTO> getDatosGraficaVentas(@RequestParam(name = "periodo", defaultValue = "semana") String periodo) {
        try {
            return ResponseEntity.ok(dashboardService.getDatosGraficaVentasNetas(periodo));
        } catch (Exception e) {
            logger.error("Error al obtener datos para la gráfica de ventas con periodo: {}", periodo, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========================================================================
    // ✅ ENDPOINT DE PREDICCIÓN ANTIGUO ELIMINADO
    // ========================================================================
    // ... El método getDatosPrediccion() ha sido eliminado ...
    // ========================================================================
}