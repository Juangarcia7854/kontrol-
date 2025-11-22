package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import com.tusistema.sistemaventas.model.DetalleVenta;
import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.model.Venta;
import com.tusistema.sistemaventas.service.EmpresaService;
import com.tusistema.sistemaventas.service.VentaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set; // Importar Set si no estaba

@Controller
@RequestMapping("/ventas")
// --- 👇 ANOTACIÓN MODIFICADA ---
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR', 'ROLE_DEMO')") 
public class VentaController {

    private final VentaService ventaService;
    private final EmpresaService empresaService;

    @Autowired
    public VentaController(VentaService ventaService, EmpresaService empresaService) {
        this.ventaService = ventaService;
        this.empresaService = empresaService;
    }

    @GetMapping
    public String listarVentas(
            @RequestParam(name = "termino", required = false) String termino,
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(name = "estado", required = false) String estado,
            Model model) {

        List<Venta> ventasFiltradas = ventaService.buscarVentas(termino, fechaDesde, fechaHasta, estado);
        
        model.addAttribute("ventas", ventasFiltradas);
        model.addAttribute("pageTitle", "Historial de Ventas");
        
        // Mantener los filtros en la vista
        model.addAttribute("termino", termino);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("estado", estado);

        ConfiguracionSistema config = empresaService.obtenerDatosEmpresa();
        model.addAttribute("configuracionSistema", config);
        
        return "ventas/lista-ventas";
    }

    // Nota: Este método POST probablemente no debería ser accesible por ROLE_DEMO
    // Podrías añadir una anotación @PreAuthorize aquí para restringirlo más si es necesario.
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR')") // Ejemplo
    @PostMapping("/guardar")
    public String guardarVenta(HttpServletRequest request,
                               @AuthenticationPrincipal Usuario usuarioLogueado,
                               RedirectAttributes redirectAttributes) {

        // --- Añadir restricción específica para demo si no deben guardar ---
         if (usuarioLogueado.getRoles().contains("ROLE_DEMO")) {
             redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios de demostración no pueden registrar ventas.");
             return "redirect:/pos"; // O a donde corresponda
         }
        // --- Fin restricción ---

        try {
            String clienteId = request.getParameter("clienteId");
            if (clienteId == null || clienteId.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar un cliente.");
            }

            List<DetalleVenta> detalles = new ArrayList<>();
            int i = 0;
            while (true) {
                String productoId = request.getParameter("detalles[" + i + "].productoId");
                if (productoId == null) { break; }
                if (productoId.isEmpty()) { i++; continue; }
                
                int cantidad = Integer.parseInt(request.getParameter("detalles[" + i + "].cantidad"));
                BigDecimal precioUnitario = new BigDecimal(request.getParameter("detalles[" + i + "].precioUnitario"));

                DetalleVenta detalle = new DetalleVenta();
                detalle.setProductoId(productoId);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                // Asegúrate de calcular el subtotal si es necesario en el DetalleVenta
                // detalle.setSubtotal(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
                detalles.add(detalle);
                i++;
            }

            if (detalles.isEmpty()) {
                throw new IllegalArgumentException("La venta debe tener al menos un producto.");
            }

            Venta nuevaVenta = new Venta();
            // Asignar cliente ID si tu modelo Venta lo tiene
            // nuevaVenta.setClienteId(clienteId); 
            nuevaVenta.setDetalles(detalles);
            // El servicio calculará el total y asignará fecha, vendedor, etc.
            
            Venta ventaGuardada = ventaService.crearVenta(nuevaVenta, clienteId, usuarioLogueado.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Venta registrada exitosamente con N° Factura: " + ventaGuardada.getNumeroFactura());
            
            return "redirect:/ventas/" + ventaGuardada.getId();

        } catch (NumberFormatException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar cantidades o precios.");
             return "redirect:/pos";
        } catch (IllegalArgumentException e) {
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
             return "redirect:/pos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al registrar la venta.");
            // Loggear el error completo en el servidor
            // logger.error("Error al guardar venta:", e); 
            return "redirect:/pos";
        }
    }

    @GetMapping("/{id}")
    public String verDetalleVenta(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Venta> ventaOptional = ventaService.obtenerVentaPorId(id);
        if (ventaOptional.isPresent()) {
            Venta venta = ventaOptional.get();
            model.addAttribute("venta", venta);
            model.addAttribute("pageTitle", "Detalle de Venta - " + venta.getNumeroFactura());
            ConfiguracionSistema config = empresaService.obtenerDatosEmpresa();
            model.addAttribute("configuracionSistema", config);
            return "ventas/detalle-venta";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Venta no encontrada.");
            return "redirect:/ventas";
        }
    }
}