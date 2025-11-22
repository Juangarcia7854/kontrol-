package com.tusistema.sistemaventas.controller; // Asegúrate que el paquete sea el correcto

import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/usuarios") // Ruta base para la gestión de usuarios
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private static final String DEFAULT_ROLE_FALLBACK_CONTROLLER = "ROLE_VENDEDOR"; // Coincide con el del servicio

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("pageTitle", "Gestión de Usuarios");
        return "admin/usuarios/lista-usuarios";
    }

     @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("allRoles", usuarioService.obtenerTodosLosRolesDisponibles());
        model.addAttribute("pageTitle", "Nuevo Usuario");
        model.addAttribute("editMode", false);
        return "admin/usuarios/form-usuario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
                                 BindingResult bindingResult,
                                 @RequestParam(name = "rawPassword", required = false) String rawPassword,
                                 @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                                 @RequestParam(name = "selectedRoles", required = false) Set<String> selectedRoles,
                                 Model model, RedirectAttributes redirectAttributes) {
        boolean isNewUser = (usuario.getId() == null || usuario.getId().isEmpty());

        if (isNewUser || (usuario.getUsername() != null && !usuario.getUsername().isEmpty())) {
            Optional<Usuario> existentePorUsername = usuarioService.obtenerUsuarioPorUsername(usuario.getUsername());
            if (existentePorUsername.isPresent() && (isNewUser || !existentePorUsername.get().getId().equals(usuario.getId()))) {
                bindingResult.addError(new FieldError("usuario", "username", "El nombre de usuario '" + usuario.getUsername() + "' ya está en uso."));
            }
        }

        if (isNewUser && (rawPassword == null || rawPassword.trim().isEmpty())) {
            bindingResult.addError(new FieldError("usuario", "password", "La contraseña es obligatoria para nuevos usuarios."));
        }

        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            if (confirmPassword == null || !rawPassword.equals(confirmPassword)) {
                 bindingResult.addError(new FieldError("usuario", "password", "Las contraseñas no coinciden."));
            }
        }

        if (selectedRoles != null && !selectedRoles.isEmpty()) {
            usuario.setRoles(selectedRoles);
        } else {
            // Si no se seleccionan roles, el servicio asignará el rol por defecto.
            // No obstante, si quisiéramos un fallback explícito aquí (aunque es redundante si el servicio lo maneja bien):
            // usuario.setRoles(Set.of(DEFAULT_ROLE_FALLBACK_CONTROLLER));
            // O, si selectedRoles es null pero el usuario ya tiene roles (edición sin cambiar roles), no hacer nada aquí
            // y dejar que el servicio maneje la lógica de si se mantienen o no.
            // La lógica actual del servicio es la preferida para la asignación de roles.
            // Este bloque 'else' podría incluso eliminarse si confiamos plenamente en el servicio.
            // Para ser explícitos por si el `selectedRoles` llega como Set vacío:
            if (selectedRoles != null && selectedRoles.isEmpty()){
                 // El servicio manejará esto asignando el DEFAULT_ROLE
                 usuario.setRoles(Set.of()); // Se envía vacío para que el servicio aplique el default
            } else if (selectedRoles == null && isNewUser) {
                 usuario.setRoles(Set.of(DEFAULT_ROLE_FALLBACK_CONTROLLER)); // Asegura un rol para el nuevo usuario si no se envía nada
            }
            // Si es edición y selectedRoles es null, se mantendrán los roles existentes si no se pasa `rawPassword`
            // o el servicio los validará si se pasa `rawPassword`.
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", usuarioService.obtenerTodosLosRolesDisponibles());
            model.addAttribute("pageTitle", isNewUser ? "Nuevo Usuario" : "Editar Usuario");
            model.addAttribute("editMode", !isNewUser);
            model.addAttribute("selectedRoles", selectedRoles != null ? selectedRoles : (usuario.getRoles() != null ? usuario.getRoles() : Set.of(DEFAULT_ROLE_FALLBACK_CONTROLLER)));
            return "admin/usuarios/form-usuario";
        }

        try {
            usuarioService.guardarUsuario(usuario, rawPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario: " + e.getMessage());
            model.addAttribute("allRoles", usuarioService.obtenerTodosLosRolesDisponibles());
            model.addAttribute("pageTitle", isNewUser ? "Nuevo Usuario" : "Editar Usuario");
            model.addAttribute("editMode", !isNewUser);
            model.addAttribute("selectedRoles", selectedRoles != null ? selectedRoles : (usuario.getRoles() != null ? usuario.getRoles() : Set.of(DEFAULT_ROLE_FALLBACK_CONTROLLER)));
            return "admin/usuarios/form-usuario";
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarUsuario(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOptional = usuarioService.obtenerUsuarioPorId(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            model.addAttribute("usuario", usuario);
            model.addAttribute("allRoles", usuarioService.obtenerTodosLosRolesDisponibles());
            model.addAttribute("selectedRoles", usuario.getRoles());
            model.addAttribute("pageTitle", "Editar Usuario");
            model.addAttribute("editMode", true);
            return "admin/usuarios/form-usuario";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable String id, RedirectAttributes redirectAttributes,
                                  @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario currentUser) {
        if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "No puedes eliminar tu propia cuenta.");
            return "redirect:/admin/usuarios";
        }
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}