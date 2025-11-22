package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Proveedor;
import com.tusistema.sistemaventas.service.ProveedorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importar RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page; // Importar Page

import java.util.Optional;

@Controller
@RequestMapping("/proveedores")
@PreAuthorize("isAuthenticated()")
public class ProveedorController {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorController.class);
    private final ProveedorService proveedorService;

    @Autowired
    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // **[ACTUALIZADO]** Listado con Paginación y Búsqueda
    @GetMapping
    public String listarProveedores(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String term,
        Model model
    ) {
        logger.info("Listando proveedores activos. Página: {}, Tamaño: {}, Búsqueda: '{}'", page, size, term);
        
        Page<Proveedor> proveedoresPage = proveedorService.obtenerProveedoresActivosPaginated(page, size, term);
        
        model.addAttribute("proveedores", proveedoresPage.getContent());
        
        // Atributos de paginación para la vista
        model.addAttribute("page", proveedoresPage); 
        model.addAttribute("currentPage", proveedoresPage.getNumber());
        model.addAttribute("totalPages", proveedoresPage.getTotalPages());
        model.addAttribute("totalItems", proveedoresPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("searchTerm", term); 
        
        model.addAttribute("pageTitle", "Lista de Proveedores");
        
        return "proveedores/lista-proveedores";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GESTOR')")
    public String mostrarFormularioNuevoProveedor(Model model) {
        if (!model.containsAttribute("proveedor")) {
            model.addAttribute("proveedor", new Proveedor());
        }
        model.addAttribute("pageTitle", "Registrar Nuevo Proveedor");
        model.addAttribute("editMode", false);
        return "proveedores/form-proveedor";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GESTOR')")
    public String guardarProveedor(@Valid @ModelAttribute("proveedor") Proveedor proveedor, BindingResult bindingResult, 
                               RedirectAttributes redirectAttributes, Model model) {
        logger.info("Intentando guardar proveedor: {}", proveedor.getNombre());
        
        // La validación de duplicados y errores está delegada y centralizada en ProveedorService,
        // por lo que aquí solo manejamos los errores de la validación @Valid (jakarta.validation)
        if (bindingResult.hasErrors()) {
            logger.warn("Errores de validación @Valid al guardar proveedor: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", proveedor.getId() == null || proveedor.getId().isEmpty() ? "Registrar Nuevo Proveedor" : "Editar Proveedor");
            model.addAttribute("editMode", proveedor.getId() != null && !proveedor.getId().isEmpty());
            return "proveedores/form-proveedor";
        }

        try {
            proveedorService.guardarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("successMessage", "Proveedor guardado exitosamente!");
        } catch (IllegalArgumentException e) {
             logger.error("Error de lógica de negocio al guardar proveedor: {}", e.getMessage());
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
             redirectAttributes.addFlashAttribute("proveedor", proveedor); // Mantener datos
             
             // Determinar a qué formulario redirigir
             String redirectUrl = (proveedor.getId() != null && !proveedor.getId().isEmpty()) ? 
                                  "/proveedores/editar/" + proveedor.getId() : 
                                  "/proveedores/nuevo";
             return "redirect:" + redirectUrl; 
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el proveedor: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al guardar el proveedor. Intente de nuevo.");
            redirectAttributes.addFlashAttribute("proveedor", proveedor);
            return "redirect:/proveedores/nuevo";
        }
        
        return "redirect:/proveedores";
    }
    
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GESTOR')")
    public String mostrarFormularioEditarProveedor(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedorOpt = proveedorService.obtenerProveedorPorId(id);
        
        if (proveedorOpt.isPresent()) {
            model.addAttribute("proveedor", proveedorOpt.get());
            model.addAttribute("pageTitle", "Editar Proveedor");
            model.addAttribute("editMode", true);
            return "proveedores/form-proveedor";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Proveedor no encontrado.");
            return "redirect:/proveedores";
        }
    }


    // **[NUEVO]** Eliminación Lógica (Soft Delete)
    // REEMPLAZA el método @PostMapping("/eliminar/{id}")
    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String desactivarProveedor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        logger.info("Intentando DESACTIVAR proveedor con ID: {}", id);
        if (id == null || id.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede desactivar proveedor: ID no proporcionado.");
            return "redirect:/proveedores";
        }
        try {
            proveedorService.desactivarProveedor(id);
            logger.info("Proveedor DESACTIVADO (Soft Delete) exitosamente con ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Proveedor desactivado exitosamente!");
        } catch (IllegalArgumentException e) {
            logger.error("Error al desactivar el proveedor con ID '{}': {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); 
        } catch (Exception e) {
            logger.error("Error inesperado al desactivar el proveedor con ID '{}': {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al desactivar el proveedor. Intente de nuevo.");
        }
        return "redirect:/proveedores";
    }
}