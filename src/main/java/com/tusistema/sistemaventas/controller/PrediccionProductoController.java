package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.dto.PrediccionProductoDTO;
import com.tusistema.sistemaventas.model.Inventario;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.repository.InventarioRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository;
import com.tusistema.sistemaventas.service.ModelTrainingService;
import com.tusistema.sistemaventas.service.PrediccionService;
import com.tusistema.sistemaventas.service.PrediccionService.ResultadoIA; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/pruebas-prediccion") 
@PreAuthorize("hasRole('ADMIN')")
public class PrediccionProductoController {

    @Autowired private PrediccionService prediccionService;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ModelTrainingService trainingService;

    @GetMapping
    public String mostrarPaginaDePruebas(Model model, @RequestParam(required = false) String productoId) {
        model.addAttribute("productos", productoRepository.findAll());
        
        if (productoId != null && !productoId.isEmpty()) {
            try {
                Producto p = productoRepository.findById(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                
                PrediccionProductoDTO dto = new PrediccionProductoDTO(p);

                // 1. Datos
                int stockActual = inventarioRepository.findByProductoId(p.getId()).map(Inventario::getCantidad).orElse(0);
                double precio = p.getPrecio().doubleValue();
                String categoria = p.getCategoria();
                int ventas7d = trainingService.getVentasAgregadas(7).getOrDefault(p.getId(), 0);
                int ventas30d = trainingService.getVentasAgregadas(30).getOrDefault(p.getId(), 0);
                int totalVendido = trainingService.getVentasAgregadas(365).getOrDefault(p.getId(), 0);
                int totalDevuelto = trainingService.getDevolucionesAgregadas(365).getOrDefault(p.getId(), 0);
                double tasaDevolucion = (totalVendido == 0) ? 0.0 : (double) totalDevuelto / totalVendido;
                long diasSinVentas = trainingService.getDiasDesdeUltimaVenta().getOrDefault(p.getId(), 999L);

                // 2. Predicciones y Recomendaciones
                
                // --- Venta Rápida ---
                ResultadoIA rVenta = prediccionService.predecirVentaRapida(stockActual, ventas7d, precio);
                dto.setSeVendePronto(rVenta.valor);
                dto.setConfianzaVenta(String.format("%.1f%%", rVenta.confianza * 99.99));
                
                if ("SI".equals(rVenta.valor)) {
                    dto.setRecomendacionVenta("¡Alta Demanda! Asegura el stock.");
                } else {
                    dto.setRecomendacionVenta("Rotación lenta. No sobre-stockear.");
                }

                // --- Devolución ---
                ResultadoIA rDev = prediccionService.predecirDevolucion(precio, categoria, tasaDevolucion);
                dto.setRiesgoDevolucion(rDev.valor);
                dto.setConfianzaDevolucion(String.format("%.1f%%", rDev.confianza * 100));
                
                if ("SI".equals(rDev.valor)) {
                    dto.setRecomendacionDevolucion("Revisar calidad o descripción.");
                } else {
                    dto.setRecomendacionDevolucion("Producto seguro. Baja tasa de devolución.");
                }

                // --- Promoción ---
                ResultadoIA rPromo = prediccionService.predecirPromocion(stockActual, ventas30d, (int)diasSinVentas);
                dto.setNecesitaPromocion(rPromo.valor);
                dto.setConfianzaPromocion(String.format("%.1f%%", rPromo.confianza * 100));
                
                if ("SI".equals(rPromo.valor)) {
                    dto.setRecomendacionPromocion("Sugerencia: Aplicar descuento del 15%.");
                } else {
                    dto.setRecomendacionPromocion("Precio adecuado. Mantener estrategia.");
                }

                String detalles = String.format("Stock: %d | Ventas(7d): %d | Tasa Dev: %.1f%%",
                    stockActual, ventas7d, tasaDevolucion * 100);
                dto.setDetalles(detalles);
                
                model.addAttribute("resultado", dto);

            } catch (Exception e) {
                e.printStackTrace();
                PrediccionProductoDTO errorDTO = new PrediccionProductoDTO(null);
                errorDTO.setError("Error: " + e.getMessage());
                model.addAttribute("resultado", errorDTO);
            }
        }
        return "admin/prediccion-producto"; 
    }
}