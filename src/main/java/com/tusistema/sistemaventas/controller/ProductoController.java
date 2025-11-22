package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.dto.ProductoVistaDTO;
import com.tusistema.sistemaventas.model.Inventario;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.repository.InventarioRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository; 
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections; 
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/productos")
@PreAuthorize("isAuthenticated()")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    private final ProductoService productoService;
    private final InventarioRepository inventarioRepository; 
    private final ProductoRepository productoRepository; 

    @Autowired
    public ProductoController(ProductoService productoService, InventarioRepository inventarioRepository, ProductoRepository productoRepository) {
        this.productoService = productoService;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository; 
    }

    // --- ⬇️ MÉTODO CORREGIDO (SIMPLE Y DIRECTO) ⬇️ ---
    @GetMapping
    public String listarProductos(Model model) {
        // Obtenemos los productos directamente del servicio.
        // Como ya actualizamos 'InventarioService', estos productos TIENEN el stock correcto.
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        
        model.addAttribute("productos", productos); 
        model.addAttribute("pageTitle", "Lista de Productos");
        
        return "productos/lista-productos";
    }
    // --- ⬆️ FIN DE LA CORRECCIÓN ⬆️ ---


    // API para el POS (Aquí sí podemos usar el DTO si quieres, o cambiarlo también)
    @GetMapping("/api/buscar")
    @ResponseBody 
    public ResponseEntity<List<ProductoVistaDTO>> buscarProductosParaPos(@RequestParam("term") String term) {
        if (term == null || term.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Producto> productosEncontrados = productoRepository.findByNombreContainingIgnoreCase(term);
        
        List<ProductoVistaDTO> resultado = productosEncontrados.stream()
            .map(p -> {
                // Ahora usamos p.getStock() que es el campo real
                return ProductoVistaDTO.fromProducto(p, p.getStock()); 
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    // ... (Resto de métodos sin cambios: nuevo, editar, guardar, eliminar) ...
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("pageTitle", "Nuevo Producto");
        model.addAttribute("editMode", false);
        return "productos/form-producto";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Producto> productoOptional = productoService.obtenerProductoPorId(id);
        if (productoOptional.isPresent()) {
            model.addAttribute("producto", productoOptional.get());
            model.addAttribute("pageTitle", "Editar Producto");
            model.addAttribute("editMode", true);
            model.addAttribute("imagenUrlActual", productoOptional.get().getImagenUrl());
            return "productos/form-producto";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
        return "redirect:/productos";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                  BindingResult bindingResult,
                                  @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                  @AuthenticationPrincipal Usuario usuarioLogueado,
                                  Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Error en el Formulario");
            model.addAttribute("editMode", producto.getId() != null);
            return "productos/form-producto";
        }
        try {
            productoService.guardarProductoConImagen(producto, imagenFile, usuarioLogueado.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Producto guardado.");
            return "redirect:/productos";
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Error en el Formulario");
            model.addAttribute("editMode", producto.getId() != null);
            model.addAttribute("errorMessage", "Error al guardar: " + e.getMessage());
            return "productos/form-producto";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/productos";
    }
}