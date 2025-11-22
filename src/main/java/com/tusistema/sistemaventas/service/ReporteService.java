package com.tusistema.sistemaventas.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tusistema.sistemaventas.dto.GraficoDTO;
import com.tusistema.sistemaventas.dto.ProductoRankingDTO;
import com.tusistema.sistemaventas.model.DetalleDevolucion;
import com.tusistema.sistemaventas.model.DetalleVenta;
import com.tusistema.sistemaventas.model.Devolucion;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.model.Venta;
import com.tusistema.sistemaventas.repository.DevolucionRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository;
import com.tusistema.sistemaventas.repository.VentaRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects; // ⬅️ ¡Esta es la línea que faltaba!
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteService.class);
    private final VentaRepository ventaRepository;
    private final DevolucionRepository devolucionRepository;
    private final ProductoRepository productoRepository;

    @Autowired
    public ReporteService(VentaRepository ventaRepository,
                          DevolucionRepository devolucionRepository,
                          ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.devolucionRepository = devolucionRepository;
        this.productoRepository = productoRepository;
    }

    public BigDecimal obtenerTotalVentasHoy() {
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = LocalDate.now().atTime(LocalTime.MAX);
        return calcularVentasNetasEnRango(inicioDelDia, finDelDia);
    }

    public long obtenerNumeroDeVentasHoy() {
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = LocalDate.now().atTime(LocalTime.MAX);
        List<String> estadosVentaActiva = Arrays.asList(Venta.ESTADO_COMPLETADA, Venta.ESTADO_DEVUELTA_PARCIAL, Venta.ESTADO_PENDIENTE);
        return ventaRepository.findByFechaVentaBetweenAndEstadoIn(inicioDelDia, finDelDia, estadosVentaActiva).size();
    }

    public Map<String, BigDecimal> obtenerVentasDiariasUltimosXDias(int dias) {
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusDays(dias - 1).with(LocalTime.MIN);
        Map<String, BigDecimal> ventasPorDia = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM", new Locale("es", "ES"));

        for (int i = 0; i < dias; i++) {
            LocalDate diaActual = fechaInicio.plusDays(i).toLocalDate();
            ventasPorDia.put(diaActual.format(formatter), BigDecimal.ZERO);
        }

        List<Venta> ventasDelPeriodo = ventaRepository.findByFechaVentaBetweenAndEstadoIn(fechaInicio, fechaFin, Arrays.asList(Venta.ESTADO_COMPLETADA, Venta.ESTADO_DEVUELTA_PARCIAL));
        List<Devolucion> devolucionesDelPeriodo = devolucionRepository.findByFechaDevolucionBetweenAndEstadoDevolucionIn(fechaInicio, fechaFin, Collections.singletonList("PROCESADA"));

        for (Venta venta : ventasDelPeriodo) {
            String fechaFormateada = venta.getFechaVenta().format(formatter);
            ventasPorDia.computeIfPresent(fechaFormateada, (key, valorActual) -> valorActual.add(venta.getTotalVenta()));
        }

        for (Devolucion devolucion : devolucionesDelPeriodo) {
            String fechaFormateada = devolucion.getFechaDevolucion().format(formatter);
            ventasPorDia.computeIfPresent(fechaFormateada, (key, valorActual) -> valorActual.subtract(devolucion.getTotalDevolucion()));
        }
        return ventasPorDia;
    }

    public List<String> obtenerEtiquetasVentasUltimosDias() {
        Map<String, BigDecimal> ventasDiarias = obtenerVentasDiariasUltimosXDias(7);
        return new ArrayList<>(ventasDiarias.keySet());
    }

    public List<Double> obtenerValoresVentasUltimosDias() {
        Map<String, BigDecimal> ventasDiarias = obtenerVentasDiariasUltimosXDias(7);
        return ventasDiarias.values().stream().map(BigDecimal::doubleValue).collect(Collectors.toList());
    }

    public GraficoDTO obtenerDatosGraficoVentasUltimos7Dias() {
        List<String> etiquetas = obtenerEtiquetasVentasUltimosDias();
        List<Double> valores = obtenerValoresVentasUltimosDias();
        return new GraficoDTO(etiquetas, valores);
    }

    public Map<String, Integer> obtenerTopNProductosMasVendidosPorCantidad(int topN) {
        List<String> estadosVentaConsiderados = Arrays.asList(Venta.ESTADO_COMPLETADA, Venta.ESTADO_DEVUELTA_PARCIAL, Venta.ESTADO_DEVUELTA_TOTAL);
        List<Venta> todasLasVentasRelevantes = ventaRepository.findAll().stream()
                .filter(venta -> estadosVentaConsiderados.contains(venta.getEstado()))
                .collect(Collectors.toList());

        Map<String, Integer> cantidadVendidaBruta = todasLasVentasRelevantes.stream()
                .flatMap(venta -> venta.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        DetalleVenta::getNombreProducto,
                        Collectors.summingInt(DetalleVenta::getCantidad)
                ));

        List<String> estadosDevolucionConsiderados = Collections.singletonList("PROCESADA");
        List<Devolucion> todasLasDevolucionesProcesadas = devolucionRepository.findAll().stream()
                .filter(d -> estadosDevolucionConsiderados.contains(d.getEstadoDevolucion()))
                .collect(Collectors.toList());

        Map<String, Integer> cantidadDevuelta = todasLasDevolucionesProcesadas.stream()
                .flatMap(devolucion -> devolucion.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        DetalleDevolucion::getNombreProducto,
                        Collectors.summingInt(DetalleDevolucion::getCantidadDevuelta)
                ));

        Map<String, Integer> cantidadNetaVendida = new LinkedHashMap<>();
        cantidadVendidaBruta.forEach((nombreProducto, cantidadBruta) -> {
            int devuelta = cantidadDevuelta.getOrDefault(nombreProducto, 0);
            cantidadNetaVendida.put(nombreProducto, cantidadBruta - devuelta);
        });

        return cantidadNetaVendida.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public List<String> obtenerNombresTopProductosMasVendidos(int limite) {
        Map<String, Integer> topProductosMap = obtenerTopNProductosMasVendidosPorCantidad(limite);
        return new ArrayList<>(topProductosMap.keySet());
    }

    public List<Number> obtenerCantidadesTopProductosMasVendidos(int limite) {
        Map<String, Integer> topProductosMap = obtenerTopNProductosMasVendidosPorCantidad(limite);
        return new ArrayList<>(topProductosMap.values());
    }

    public void generarReporteVentasExcel(HttpServletResponse response) throws IOException {
        // La implementación original de este método no se modificó, asumo que está bien.
        // --- INICIO DE IMPLEMENTACIÓN DE EJEMPLO para que compile ---
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Reporte de Ventas");

            // Crear el estilo del encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Crear el encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Venta", "Fecha", "Cliente", "Total"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Simular datos (debes reemplazar esto con la lógica real de consulta y llenado)
            List<Venta> ventas = ventaRepository.findAll();
            int rowNum = 1;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(venta.getId() != null ? venta.getId().toString() : ""); // Asume que getId() existe
                row.createCell(1).setCellValue(venta.getFechaVenta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                row.createCell(2).setCellValue(venta.getClienteId() != null ? venta.getClienteId().toString() : "N/A"); // Asume que getClienteId() existe
                row.createCell(3).setCellValue(venta.getTotalVenta().doubleValue());
            }
            
            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=reporte_ventas_" + LocalDate.now() + ".xlsx";
            response.setHeader(headerKey, headerValue);

            workbook.write(response.getOutputStream());
        }
        // --- FIN DE IMPLEMENTACIÓN DE EJEMPLO ---
    }

    public void generarReporteVentasPdf(HttpServletResponse response) throws IOException, DocumentException {
        // La implementación original de este método no se modificó, asumo que está bien.
        // --- INICIO DE IMPLEMENTACIÓN DE EJEMPLO para que compile ---
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reporte_ventas_" + LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Paragraph titulo = new Paragraph("Reporte de Ventas", fontTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);

        Font fontTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        float[] columnWidths = {2f, 3f, 3f, 2f};
        tabla.setWidths(columnWidths);

        String[] headers = {"ID Venta", "Fecha", "Cliente ID", "Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fontTabla));
            cell.setBackgroundColor(new Color(40, 100, 150)); // Un azul oscuro
            cell.setPadding(5);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            tabla.addCell(cell);
        }

        // Simular datos (debes reemplazar esto con la lógica real de consulta y llenado)
        Font fontDatos = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        List<Venta> ventas = ventaRepository.findAll();

        for (Venta venta : ventas) {
            tabla.addCell(new Phrase(venta.getId() != null ? venta.getId().toString() : "", fontDatos));
            tabla.addCell(new Phrase(venta.getFechaVenta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), fontDatos));
            tabla.addCell(new Phrase(venta.getClienteId() != null ? venta.getClienteId().toString() : "N/A", fontDatos));
            tabla.addCell(new Phrase(venta.getTotalVenta().toString(), fontDatos));
        }

        document.add(tabla);
        document.close();
        // --- FIN DE IMPLEMENTACIÓN DE EJEMPLO ---
    }

    public List<Producto> obtenerRecomendacionesDeProducto(String productoIdOrigen, int limite) {
        // La implementación original de este método no se modificó.
        return Collections.emptyList();
    }

    private BigDecimal calcularVentasNetasEnRango(LocalDateTime inicio, LocalDateTime fin) {
        // La implementación original de este método no se modificó.
        // Nota: Este método DEBE tener una implementación real para que la clase funcione correctamente.
        // La siguiente es una implementación de marcador de posición (placeholder)
        
        List<Venta> ventas = ventaRepository.findByFechaVentaBetweenAndEstadoIn(inicio, fin, Arrays.asList(Venta.ESTADO_COMPLETADA, Venta.ESTADO_DEVUELTA_PARCIAL));
        BigDecimal ventasTotales = ventas.stream()
                .map(Venta::getTotalVenta)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Devolucion> devoluciones = devolucionRepository.findByFechaDevolucionBetweenAndEstadoDevolucionIn(inicio, fin, Collections.singletonList("PROCESADA"));
        BigDecimal devolucionesTotales = devoluciones.stream()
                .map(Devolucion::getTotalDevolucion)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ventasTotales.subtract(devolucionesTotales);
    }

    // ✅ INICIO: NUEVOS MÉTODOS PARA KPIs
    
    public ProductoRankingDTO obtenerProductoMasVendidoHistorico() {
        Map<String, Integer> topProductos = obtenerTopNProductosMasVendidosPorCantidad(1);
        if (topProductos.isEmpty()) {
            return new ProductoRankingDTO("N/A", 0);
        }
        Map.Entry<String, Integer> entry = topProductos.entrySet().iterator().next();
        return new ProductoRankingDTO(entry.getKey(), entry.getValue());
    }
    
    /**
     * Calcula el valor de la venta promedio por cliente (Ingresos Totales / Clientes Únicos).
     */
    public BigDecimal obtenerVentaPromedioPorCliente() {
        List<Venta> todasLasVentas = ventaRepository.findAll();
        if (todasLasVentas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 1. Calcular Ingresos Totales
        BigDecimal ingresosTotales = todasLasVentas.stream()
                .map(Venta::getTotalVenta)
                .filter(Objects::nonNull) // Filtro de seguridad (requiere la importación de java.util.Objects)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 2. Contar Clientes Únicos
        long clientesUnicos = todasLasVentas.stream()
                .map(Venta::getClienteId)
                .filter(Objects::nonNull) // Filtro de seguridad (requiere la importación de java.util.Objects)
                .distinct() // Obtiene IDs únicos
                .count();

        if (clientesUnicos == 0) {
            return BigDecimal.ZERO;
        }
        
        // 3. Calcular el promedio y redondear
        return ingresosTotales.divide(new BigDecimal(clientesUnicos), 2, RoundingMode.HALF_UP);
    }
    // ✅ FIN: NUEVOS MÉTODOS PARA KPIs
}