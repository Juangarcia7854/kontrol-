package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.dto.DashboardUpdateDTO;
import com.tusistema.sistemaventas.dto.GraficoDTO;
import com.tusistema.sistemaventas.dto.ProductoRankingDTO;
// import com.tusistema.sistemaventas.dto.PrediccionAgotamientoDTO; // <-- ELIMINADO
import com.tusistema.sistemaventas.model.*;
// import com.tusistema.sistemaventas.repository.InventarioRepository; // <-- ELIMINADO
// import com.tusistema.sistemaventas.repository.ProductoRepository; // <-- ELIMINADO
import com.tusistema.sistemaventas.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final ReporteService reporteService;
    
    // ========================================================================
    // ✅ INICIO: DEPENDENCIAS DE WEKA ELIMINADAS
    // ========================================================================
    // private final PrediccionService prediccionService;
    // private final ProductoRepository productoRepository;
    // private final InventarioRepository inventarioRepository;
    // ========================================================================
    // ✅ FIN
    // ========================================================================

    @Autowired
    public DashboardService(VentaRepository ventaRepository, 
                            ReporteService reporteService
                            // <-- Dependencias de Weka eliminadas del constructor
                            ) {
        this.ventaRepository = ventaRepository;
        this.reporteService = reporteService;
    }

    public DashboardUpdateDTO obtenerMetricasActuales() {
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<Venta> ventasDeHoy = ventaRepository.findByFechaVentaBetween(inicioDelDia, finDelDia);

        BigDecimal ingresosHoy = ventasDeHoy.stream()
                .map(Venta::getTotalVenta)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int numeroVentasHoy = ventasDeHoy.size();

        BigDecimal ticketPromedio = numeroVentasHoy > 0
                ? ingresosHoy.divide(new BigDecimal(numeroVentasHoy), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<ProductoRankingDTO> rankingProductos = ventasDeHoy.stream()
                .flatMap(venta -> venta.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getNombreProducto(),
                        Collectors.summingInt(detalle -> detalle.getCantidad())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> new ProductoRankingDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        DashboardUpdateDTO dto = new DashboardUpdateDTO();
        dto.setIngresosHoy(ingresosHoy);
        dto.setNumeroVentasHoy(numeroVentasHoy);
        dto.setTicketPromedio(ticketPromedio);
        dto.setRankingProductos(rankingProductos);
        
        return dto;
    }

    public GraficoDTO getDatosGraficaVentasNetas(String periodo) {
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio;

        switch (periodo.toLowerCase()) {
            case "hoy":
                fechaInicio = fechaFin.with(LocalTime.MIN);
                break;
            case "mes":
                fechaInicio = fechaFin.withDayOfMonth(1).with(LocalTime.MIN);
                break;
            case "semana":
            default:
                fechaInicio = fechaFin.minusDays(6).with(LocalTime.MIN);
                break;
        }

        List<Venta> ventas = ventaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);

        Map<LocalDate, BigDecimal> ventasPorDia = ventas.stream()
                .collect(Collectors.groupingBy(
                        venta -> venta.getFechaVenta().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Venta::getTotalVenta, BigDecimal::add)
                ));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM", new Locale("es", "ES"));
        
        LocalDate diaIterador = fechaInicio.toLocalDate();
        LocalDate diaFinal = fechaFin.toLocalDate();

        while (!diaIterador.isAfter(diaFinal)) {
            labels.add(diaIterador.format(formatter));
            data.add(ventasPorDia.getOrDefault(diaIterador, BigDecimal.ZERO));
            diaIterador = diaIterador.plusDays(1);
        }

        return new GraficoDTO(labels, data);
    }

    // ========================================================================
    // ✅ MÉTODO 'obtenerPrediccionVentasWeka' ELIMINADO
    // ========================================================================
}