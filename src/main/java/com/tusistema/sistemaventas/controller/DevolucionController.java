package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Devolucion;
import com.tusistema.sistemaventas.model.DetalleDevolucion;
import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.model.Venta;
import com.tusistema.sistemaventas.model.DetalleVenta; 
import com.tusistema.sistemaventas.service.DevolucionService;
import com.tusistema.sistemaventas.service.VentaService;
import com.tusistema.sistemaventas.service.ProductoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Import para la fecha
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; 
import java.util.stream.Collectors;
import java.util.stream.Stream; 
import java.util.Comparator; 

@Controller
@RequestMapping("/devoluciones")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR', 'ROLE_GESTOR', 'ROLE_DEMO')")
public class DevolucionController {

    private static final Logger logger = LoggerFactory.getLogger(DevolucionController.class);

    private final DevolucionService devolucionService;
    private final VentaService ventaService;
    private final ProductoService productoService;

    @Autowired
    public DevolucionController(DevolucionService devolucionService,
                                  VentaService ventaService,
                                  ProductoService productoService) {
        this.devolucionService = devolucionService;
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    @GetMapping
    public String listarDevoluciones(Model model) {
        // ... (sin cambios)
        model.addAttribute("devoluciones", devolucionService.obtenerTodasLasDevoluciones());
        model.addAttribute("pageTitle", "Historial de Devoluciones");
        return "devoluciones/lista-devoluciones";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioSeleccionarVenta(Model model, @RequestParam(name = "ventaId", required = false) String ventaId) {
        // ... (sin cambios)
        logger.info("Mostrando formulario para seleccionar venta. VentaID pre-seleccionado: {}", ventaId);

        List<Venta> ventasCompletadas = ventaService.buscarVentas(null, null, null, "COMPLETADA");
        List<Venta> ventasDevueltasParcial = ventaService.buscarVentas(null, null, null, "DEVUELTA_PARCIAL");

        List<Venta> ventasElegibles = Stream.concat(ventasCompletadas.stream(), ventasDevueltasParcial.stream())
                                            .distinct() 
                                            .sorted(Comparator.comparing(Venta::getFechaVenta).reversed()) 
                                            .collect(Collectors.toList());

        model.addAttribute("ventas", ventasElegibles);
        model.addAttribute("pageTitle", "Nueva Devolución: Seleccionar Venta");

        if (ventaId != null && !ventaId.isEmpty()) {
            model.addAttribute("ventaIdSeleccionada", ventaId);
            logger.debug("Venta ID {} pasada al modelo como 'ventaIdSeleccionada'", ventaId);
        }

        return "devoluciones/seleccionar-venta-form";
    }

    @GetMapping("/api/venta-detalles/{ventaId}")
    @ResponseBody
    public ResponseEntity<?> obtenerDetallesVentaParaDevolucion(@PathVariable String ventaId) {
        // ... (sin cambios)
        Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
        return ventaOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- ⬇️ MÉTODO CON LA CORRECCIÓN ⬇️ ---
    @GetMapping("/nueva/detalles")
    public String mostrarFormularioNuevaDevolucion(@RequestParam("ventaId") String ventaId, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Mostrando formulario de detalles de devolución para Venta ID: {}", ventaId);
        try {
            Venta ventaOriginal = ventaService.obtenerVentaPorId(ventaId)
                .orElseThrow(() -> new Exception("Venta no encontrada con ID: " + ventaId));

            if ("DEVUELTA_TOTAL".equals(ventaOriginal.getEstado())) {
                 logger.warn("Intento de devolver una venta ya devuelta totalmente. Venta ID: {}", ventaId);
                 redirectAttributes.addFlashAttribute("errorMessage", "Esta venta ya ha sido marcada como 'Devuelta Total' y no puede ser procesada nuevamente.");
                 return "redirect:/devoluciones/nueva";
            }

            Devolucion devolucion = new Devolucion();
            devolucion.setVentaOriginalId(ventaOriginal.getId());
            devolucion.setNumeroFacturaVentaOriginal(ventaOriginal.getNumeroFactura());
            devolucion.setClienteId(ventaOriginal.getClienteId());
            devolucion.setNombreCliente(ventaOriginal.getNombreCliente());
            devolucion.setFechaDevolucion(LocalDateTime.now()); 

            Map<String, Integer> cantidadesOriginalesMap = new HashMap<>();
            List<DetalleDevolucion> detallesDevolucion = new ArrayList<>();

            // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
            // Añadimos una comprobación de que getDetalles() no sea null
            if (ventaOriginal.getDetalles() != null) {
                for (DetalleVenta detalleVenta : ventaOriginal.getDetalles()) {
                    DetalleDevolucion detalleDev = new DetalleDevolucion();
                    detalleDev.setProductoId(detalleVenta.getProductoId());
                    detalleDev.setNombreProducto(detalleVenta.getNombreProducto());
                    detalleDev.setPrecioUnitarioDevolucion(detalleVenta.getPrecioUnitario()); 
                    detalleDev.setCantidadDevuelta(0); 
                    
                    detallesDevolucion.add(detalleDev);
                    
                    cantidadesOriginalesMap.put(detalleVenta.getProductoId(), detalleVenta.getCantidad());
                }
            } else {
                // Si la lista de detalles es null, registramos un error y prevenimos el fallo
                logger.error("La Venta ID: {} no tiene una lista de detalles (es null).", ventaId);
                // Opcional: puedes lanzar un error si lo prefieres
                // throw new Exception("La venta original no contiene detalles.");
            }
            // --- FIN DE LA CORRECCIÓN ---

            devolucion.setDetalles(detallesDevolucion);

            model.addAttribute("devolucion", devolucion);
            model.addAttribute("cantidadesOriginalesMap", cantidadesOriginalesMap); 
            model.addAttribute("pageTitle", "Registrar Devolución - Detalles");

            // Si todo va bien, te lleva al formulario
            return "devoluciones/form-devolucion"; 

        } catch (Exception e) {
            // Si algo falla, te redirige de vuelta con un mensaje de error
            logger.error("Error al preparar el formulario de devolución: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar los datos de la venta: " + e.getMessage());
            return "redirect:/devoluciones/nueva"; 
        }
    }


    @PostMapping("/guardar")
    public String guardarDevolucion(@Valid @ModelAttribute("devolucion") Devolucion devolucion,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal Usuario usuarioLogueado,
                                      Model model, RedirectAttributes redirectAttributes) {
        
        // ... (sin cambios)
         if (usuarioLogueado != null && usuarioLogueado.getRoles().contains("ROLE_DEMO")) {
             redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios de demostración no pueden registrar devoluciones.");
             return "redirect:/devoluciones"; 
         }
        
        if (devolucion.getDetalles() == null || devolucion.getDetalles().stream().allMatch(d -> d.getCantidadDevuelta() <= 0)) {
            bindingResult.rejectValue("detalles", "devolucion.detalles.min", "Debe ingresar una cantidad a devolver para al menos un producto.");
        }

        if (bindingResult.hasErrors()) {
            logger.warn("Error de validación al guardar devolución. Errores: {}", bindingResult.getAllErrors());
            revalidarYPrepararModelo(devolucion, model); 
            model.addAttribute("pageTitle", "Registrar Devolución - Detalles");
            return "devoluciones/form-devolucion";
        }
        
        try {
            devolucionService.procesarDevolucion(devolucion, devolucion.getVentaOriginalId(), usuarioLogueado.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Devolución registrada exitosamente.");
            return "redirect:/devoluciones";
        } catch (IllegalArgumentException e) {
            logger.error("Error al procesar devolución (IllegalArgument): {}", e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al registrar la devolución: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error inesperado al registrar la devolución: " + e.getMessage());
        }

        revalidarYPrepararModelo(devolucion, model); 
        model.addAttribute("pageTitle", "Registrar Devolución - Detalles");
        return "devoluciones/form-devolucion";
    }

    private void revalidarYPrepararModelo(Devolucion devolucion, Model model) {
        // ... (sin cambios)
        try {
            Venta ventaOriginal = ventaService.obtenerVentaPorId(devolucion.getVentaOriginalId())
                .orElseThrow(() -> new Exception("Venta original no encontrada"));

            Map<String, Integer> cantidadesOriginalesMap = new HashMap<>();
            
            // --- AÑADIR MISMA VALIDACIÓN AQUÍ ---
            if (ventaOriginal.getDetalles() != null) {
                for (DetalleVenta detalleVenta : ventaOriginal.getDetalles()) {
                    cantidadesOriginalesMap.put(detalleVenta.getProductoId(), detalleVenta.getCantidad());
                }
            }
            
            model.addAttribute("cantidadesOriginalesMap", cantidadesOriginalesMap);
        } catch (Exception e) {
            logger.error("Error al re-validar modelo de devolución: {}", e.getMessage(), e);
            model.addAttribute("cantidadesOriginalesMap", new HashMap<>()); 
            if (!model.containsAttribute("errorMessage")) {
                 model.addAttribute("errorMessage", "Error al recargar datos de la venta original.");
            }
        }
    }

    @GetMapping("/{id}")
    public String verDetalleDevolucion(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
       // ... (sin cambios)
       logger.debug("Solicitando detalle de devolución ID: {}", id);
       Optional<Devolucion> devolucionOptional = devolucionService.obtenerDevolucionPorId(id);
       if (devolucionOptional.isPresent()) {
           model.addAttribute("devolucion", devolucionOptional.get());
           model.addAttribute("pageTitle", "Detalle de Devolución #" + id);
           return "devoluciones/detalle-devolucion";
       } else {
           logger.warn("No se encontró la devolución con ID: {}", id);
           redirectAttributes.addFlashAttribute("errorMessage", "No se encontró la devolución solicitada.");
           return "redirect:/devoluciones";
       }
    }
}