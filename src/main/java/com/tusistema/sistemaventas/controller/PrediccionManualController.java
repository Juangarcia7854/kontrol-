package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.dto.PrediccionProductoDTO;
import com.tusistema.sistemaventas.model.*;
import com.tusistema.sistemaventas.repository.*;
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

// Define que esta clase es un Controlador de Spring MVC (retorna vistas HTML, no JSON).
@Controller
// Establece la URL base: todo lo de aquí responderá a /admin/pruebas-producto.
@RequestMapping("/admin/pruebas-producto") 
// Seguridad: Bloquea el acceso si el usuario no tiene el rol de 'ADMIN'.
@PreAuthorize("hasRole('ADMIN')")
public class PrediccionManualController {

    // Inyección de dependencias necesarias:
    
    // 1. Servicio que contiene la lógica de IA (Weka) para predecir.
    @Autowired private PrediccionService prediccionService;
    
    // 2. Repositorios para acceder a la base de datos (Productos e Inventario).
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    
    // 3. Servicio que calcula métricas históricas (ventas pasadas, devoluciones) necesarias para la IA.
    @Autowired private ModelTrainingService trainingService;

    // Maneja la petición GET. Recibe el modelo para la vista y un ID de producto opcional.
    @GetMapping
    public String mostrarPaginaDePruebas(Model model, @RequestParam(required = false) String productoId) {
        
        // Carga TODOS los productos en el modelo para llenar el select/dropdown en la vista HTML.
        model.addAttribute("productos", productoRepository.findAll());
        
        // Verificamos si el usuario seleccionó un producto (si productoId no es nulo ni vacío).
        if (productoId != null && !productoId.isEmpty()) {
            try {
                // Buscamos el producto en la BD. Si no existe, lanzamos error.
                Producto p = productoRepository.findById(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                
                // Creamos el DTO (Objeto de Transferencia de Datos) que llevaremos a la vista con los resultados.
                PrediccionProductoDTO dto = new PrediccionProductoDTO(p);

                // --- RECOPILACIÓN DE DATOS (ATRIBUTOS) PARA LA IA ---
                // Aquí obtenemos las variables que los modelos J48/NaiveBayes necesitan para calcular.
                
                // 1. Stock actual (si no hay registro, asume 0).
                int stock = inventarioRepository.findByProductoId(p.getId()).map(Inventario::getCantidad).orElse(0);
                
                // 2. Precio del producto.
                double precio = p.getPrecio().doubleValue();
                
                // 3. Categoría del producto (atributo nominal).
                String categoria = p.getCategoria();
                
                // 4. Ventas históricas calculadas por el TrainingService (últimos 7, 30 y 365 días).
                int ventas7d = trainingService.getVentasAgregadas(7).getOrDefault(p.getId(), 0);
                int ventas30d = trainingService.getVentasAgregadas(30).getOrDefault(p.getId(), 0);
                int totalV = trainingService.getVentasAgregadas(365).getOrDefault(p.getId(), 0);
                
                // 5. Devoluciones históricas y cálculo de la Tasa de Devolución.
                int totalD = trainingService.getDevolucionesAgregadas(365).getOrDefault(p.getId(), 0);
                // Evitamos división por cero: si no hubo ventas, la tasa es 0.0.
                double tasaDev = (totalV == 0) ? 0.0 : (double) totalD / totalV;
                
                // 6. Recencia: Días desde la última vez que se vendió este producto.
                long diasSin = trainingService.getDiasDesdeUltimaVenta().getOrDefault(p.getId(), 999L);

                // --- EJECUCIÓN DE PREDICCIONES ---

                // A. MODELO 1: ¿Se venderá rápido? (Venta Rápida)
                // Enviamos stock, ventas recientes y precio al servicio de IA.
                ResultadoIA rVenta = prediccionService.predecirVentaRapida(stock, ventas7d, precio);
                
                // Guardamos los resultados en el DTO para mostrarlos en pantalla.
                dto.setSeVendePronto(rVenta.valor); // "SI" o "NO"
                dto.setConfianzaVenta(String.format("%.1f%%", rVenta.confianza * 100)); // Formato porcentaje (ej. 85.5%)
                // Lógica simple de negocio basada en la respuesta de la IA para dar un consejo humano.
                dto.setRecomendacionVenta("SI".equals(rVenta.valor) ? 
                    "Alta demanda. Monitorear stock." : "Baja demanda.");

                // B. MODELO 2: ¿Riesgo de Devolución?
                // Enviamos precio, categoría y tasa histórica de fallos.
                ResultadoIA rDev = prediccionService.predecirDevolucion(precio, categoria, tasaDev);
                
                dto.setRiesgoDevolucion(rDev.valor);
                dto.setConfianzaDevolucion(String.format("%.1f%%", rDev.confianza * 100));
                dto.setRecomendacionDevolucion("SI".equals(rDev.valor) ? 
                    "Posible defecto." : "Sin riesgo aparente.");

                // C. MODELO 3: ¿Necesita Promoción? (Stock estancado)
                // Enviamos stock, ventas mensuales y días sin vender.
                ResultadoIA rPromo = prediccionService.predecirPromocion(stock, ventas30d, (int)diasSin);
                
                dto.setNecesitaPromocion(rPromo.valor);
                dto.setConfianzaPromocion(String.format("%.1f%%", rPromo.confianza * 100));
                dto.setRecomendacionPromocion("SI".equals(rPromo.valor) ? 
                    "Sugerencia: Descuento." : "Precio OK.");

                // Agregamos un resumen de datos duros para referencia visual en la tarjeta.
                String detalles = String.format("Stock: %d | Ventas(7d): %d", stock, ventas7d);
                dto.setDetalles(detalles);
                
                // Finalmente, agregamos el objeto lleno con las predicciones al modelo.
                model.addAttribute("resultado", dto);
                
            } catch (Exception e) {
                // Si algo falla (ej. modelo no cargado o error de cálculo), lo imprime en consola.
                e.printStackTrace();
            }
        }
        // Retorna el nombre de la plantilla HTML (src/main/resources/templates/admin/prediccion-producto.html).
        return "admin/prediccion-producto"; 
    }
}