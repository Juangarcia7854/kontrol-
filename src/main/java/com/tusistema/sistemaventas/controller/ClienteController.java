package com.tusistema.sistemaventas.controller; // Asegúrate que este sea tu paquete correcto

import com.tusistema.sistemaventas.model.Cliente; // Asegúrate que este sea tu paquete correcto
import com.tusistema.sistemaventas.service.ClienteService; // Asegúrate que este sea tu paquete correcto
import jakarta.validation.Valid;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/clientes")
@PreAuthorize("isAuthenticated()")
public class ClienteController {

    // Logger para esta clase
    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listarClientes(Model model) {
        logger.info("Accediendo a listarClientes");
        List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("pageTitle", "Lista de Clientes");
        return "clientes/lista-clientes";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR', 'ROLE_GESTOR')")
    public String mostrarFormularioNuevoCliente(Model model) {
        logger.info("Accediendo a mostrarFormularioNuevoCliente");
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("pageTitle", "Nuevo Cliente");
        return "clientes/form-cliente";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR', 'ROLE_GESTOR')")
    public String guardarCliente(@Valid @ModelAttribute("cliente") Cliente cliente,
                                 BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Intentando guardar cliente con ID: {}", cliente.getId());

        if (cliente.getNumeroDocumento() != null && !cliente.getNumeroDocumento().isEmpty()) {
            Optional<Cliente> existentePorDocumento = clienteService.obtenerClientePorNumeroDocumento(cliente.getNumeroDocumento());
            if (existentePorDocumento.isPresent() && (cliente.getId() == null || cliente.getId().isEmpty() || !existentePorDocumento.get().getId().equals(cliente.getId()))) {
                bindingResult.rejectValue("numeroDocumento", "error.cliente", "Ya existe un cliente con este número de documento.");
                logger.warn("Error de validación: Documento duplicado para {}", cliente.getNumeroDocumento());
            }
        }

        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
             Optional<Cliente> existentePorEmail = clienteService.obtenerClientePorEmail(cliente.getEmail());
            if (existentePorEmail.isPresent() && (cliente.getId() == null || cliente.getId().isEmpty() || !existentePorEmail.get().getId().equals(cliente.getId()))) {
                bindingResult.rejectValue("email", "error.cliente", "Ya existe un cliente con este correo electrónico.");
                logger.warn("Error de validación: Email duplicado para {}", cliente.getEmail());
            }
        }

        if (bindingResult.hasErrors()) {
            logger.warn("Errores de validación al guardar cliente: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", (cliente.getId() == null || cliente.getId().isEmpty()) ? "Nuevo Cliente" : "Editar Cliente");
            return "clientes/form-cliente";
        }

        try {
            clienteService.guardarCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente guardado exitosamente!");
            logger.info("Cliente guardado exitosamente con ID: {}", cliente.getId());
        } catch (Exception e) {
            logger.error("Error al guardar el cliente: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el cliente: " + e.getMessage());
            model.addAttribute("pageTitle", (cliente.getId() == null || cliente.getId().isEmpty()) ? "Nuevo Cliente" : "Editar Cliente");
            return "clientes/form-cliente"; // Devuelve al formulario en caso de error para mostrar el mensaje
        }
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR', 'ROLE_GESTOR')")
    public String mostrarFormularioEditarCliente(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Accediendo a mostrarFormularioEditarCliente con ID: '{}'", id);

        if (id == null || id.trim().isEmpty() || id.equalsIgnoreCase("null") || id.equalsIgnoreCase("undefined")) {
            logger.warn("ID de cliente inválido recibido: '{}'", id);
            redirectAttributes.addFlashAttribute("errorMessage", "ID de cliente inválido proporcionado.");
            return "redirect:/clientes";
        }
        Optional<Cliente> clienteOptional = clienteService.obtenerClientePorId(id);
        if (clienteOptional.isPresent()) {
            logger.info("Cliente encontrado con ID: {}", id);
            model.addAttribute("cliente", clienteOptional.get());
            model.addAttribute("pageTitle", "Editar Cliente");
            return "clientes/form-cliente";
        } else {
            logger.warn("Cliente no encontrado con ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado con ID: " + id);
            return "redirect:/clientes";
        }
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String eliminarCliente(@PathVariable String id, RedirectAttributes redirectAttributes) {
        logger.info("Intentando eliminar cliente con ID: '{}'", id);
         if (id == null || id.trim().isEmpty() || id.equalsIgnoreCase("null") || id.equalsIgnoreCase("undefined")) {
            logger.warn("ID de cliente inválido para eliminar: '{}'", id);
            redirectAttributes.addFlashAttribute("errorMessage", "ID de cliente inválido para eliminar.");
            return "redirect:/clientes";
        }
        try {
            clienteService.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado exitosamente!");
            logger.info("Cliente eliminado exitosamente con ID: {}", id);
        } catch (Exception e) { // Considera capturar excepciones más específicas si es necesario
            logger.error("Error al eliminar cliente con ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el cliente. Es posible que esté asociado a ventas u otros registros.");
        }
        return "redirect:/clientes";
    }
}